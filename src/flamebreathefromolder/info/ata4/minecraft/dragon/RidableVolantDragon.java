/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon;

import info.ata4.minecraft.GameUtils;
import info.ata4.minecraft.MathF;
import info.ata4.minecraft.dragonegg.DragonEgg;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.*;

/**
 * A ridable flying dragon.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class RidableVolantDragon extends VolantDragon {
    
    private static final Logger L = mod_DragonMounts.getLogger();
    
    private String mountPlayerName;
    private boolean isSaddled;
    private int touchedWaterUnmounted;
    private int controlMode = mod_DragonMounts.controlMode;
    private final float maxMoveSpeed = 0.6f;
    
    float yMountedOffset;
    
    public RidableVolantDragon(World world) {
        super(world);

        moveSpeed = maxMoveSpeed;
        noClip = false;
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        setSaddled(nbt.getBoolean("Saddle"), false);
        mountPlayerName = nbt.getString("MountPlayer");
        moveSpeed = nbt.getFloat("Speed");
        
        if (mountPlayerName.equals("")) {
            mountPlayerName = null;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("Saddle", isSaddled());
        nbt.setString("MountPlayer", getRider() == null ? "" : getRider().username);
        nbt.setFloat("Speed", moveSpeed);
    }
    
    @Override
    public int getMaxHealth() {
        return 80;
    }
    
    @Override
    public float getBodySize() {
        return 0.8f;
    }
    
    @Override
    public boolean isSaddled() {
        return isSaddled;
    }

    public void setSaddled(boolean saddled, boolean showParticles) {
        if (showParticles && saddled && isSaddled) {
            for (int i = 0; i < 16; i++) {
                worldObj.spawnParticle("explode", posX, posY + yMountedOffset, posZ, 0, 0, 0);
            }
        }

        isSaddled = saddled;
        
        L.log(Level.FINE, "Dragon {0} is now {1}", new Object[]{entityId, saddled ? "saddled" : "unsaddled"});
    }
    
    public void setSaddled(boolean saddled) {
        setSaddled(saddled, true);
    }
    
    @Override
    public boolean canBeCollidedWith() {
        // disable collision while the rider is using the bow
        if (getRider() != null) {
            ItemStack playerItem = getRider().getCurrentEquippedItem();
            if (playerItem != null && playerItem.getItem().shiftedIndex == Item.bow.shiftedIndex) {
                return false;
            }
        }

        return super.canBeCollidedWith();
    }
    
    public EntityPlayer getRider() {
        if (riddenByEntity != null && riddenByEntity instanceof EntityPlayer) {
            return (EntityPlayer)riddenByEntity;
        } else {
            return null;
        }
    }

    public void setRider(EntityPlayer player) {
        if (player != null && !player.isDead) {
            L.log(Level.FINE, "Dragon {0} {1} {2}", new Object[]{entityId, riddenByEntity == player ? "unmounted" : "mounted", player.username});
            player.mountEntity(this);
            
            if (riddenByEntity == null) {
                // perform soft unmounting by teleporting the player close to the
                // ground in the direction he/she is looking at
                Vec3D look = player.getLookVec();
                player.setPosition(player.posX + look.xCoord * 2, player.posY - 2, player.posZ + look.zCoord * 2);
            } else if (!isSaddled()) {
                // tell the player about the inconveniences of unsaddled dragons
                player.addChatMessage("mod.dragonmount.unsaddled");
            }
        } else {
            L.log(Level.FINE, "Dragon {0} rider is invalid or dead!", entityId);
            transformToEgg(null);
        }
    }
    
    @Override
    public boolean handleWaterMovement() {
        // bounce off water surfaces, but allow partial submersion
        if (worldObj.handleMaterialAcceleration(boundingBox.contract(0, 1.5, 0), Material.water, this)) {
            motionY = 0.5f;
            targetY = posY + 5;
            
            // don't bounce off too often when unmounted
            if (getRider() == null) {
                if (touchedWaterUnmounted++ >= 10) {
                    L.log(Level.FINE, "Dragon {0} was stuck on a water surface!", entityId);
                    transformToEgg(null);
                    return false;
                }
            } else {
                touchedWaterUnmounted = 0;
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void updateEntityActionState() {
        // re-mount player
        if (mountPlayerName != null) {
            L.log(Level.FINE, "Dragon {0} re-mounts player {1}", new Object[]{entityId, mountPlayerName});
            EntityPlayer player = worldObj.getPlayerEntityByName(mountPlayerName);
            setRider(player);
            mountPlayerName = null;
        }
        
        // randomize target only if unsaddled or unmounted
        randomTarget = derangeTarget = !isSaddled() || riddenByEntity == null;
        
        updateRiderControl();
        
        super.updateEntityActionState();
    }
    
    @Override
    public void updateAirActionState() {
        // protect dragon and rider from fall damage
        fallDistance = 0;
        
        super.updateAirActionState();
    }
    
    @Override
    protected void updateGroundActionState() {
        super.updateGroundActionState();
        
        // don't move!
        motionX = 0;
        motionY = 0;
        motionZ = 0;

        // don't rotate!
        prevRotationPitch = rotationPitch = 0;
        prevRotationYaw = rotationYaw;
        randomYawVelocity = 0;

        if (hurtTime > 0) {
            moveEntity(0, 1, 0);
            if (riddenByEntity == null) {
                // that's it, I'm out of here!
                targetY = 120;
            }
        }
    }
    
    protected void updateRiderControl() {
        if (worldObj.multiplayerWorld) {
            return;
        }
        
        EntityPlayer riderTmp = getRider();

        if (riderTmp == null || !(riderTmp instanceof EntityPlayerSP)) {
            // nothing there that could control me
            return;
        }
        
        EntityPlayerSP rider = (EntityPlayerSP) riderTmp;

        Vec3D riderLook = rider.getLookVec();
        Vec3D dragonLook = getLookVec();
        
        float targetDist = 8;
        
        // control angles only when saddled
        if (isSaddled()) {
            if (controlMode == 1) {
                // control angles by strafing
                
                // set target directly in front of the dragon
                targetX = posX + dragonLook.xCoord * targetDist;
                targetZ = posZ + dragonLook.zCoord * targetDist;

                if (rider.movementInput.moveStrafe != 0) {
                    double yawAngles = (rotationYaw * Math.PI) / 180d;
                    double rotMulti = rider.movementInput.moveStrafe * targetDist * 0.5;
                    double rotX = Math.cos(yawAngles) * rotMulti;
                    double rotZ = Math.sin(yawAngles) * rotMulti;

                    targetX -= rotX;
                    targetZ -= rotZ;
                }
            } else {
                // control angles by mouse look
                targetX = posX + riderLook.xCoord * targetDist;
                targetZ = posZ + riderLook.zCoord * targetDist;
            }
        }

        if (controlMode == 0) {
            // control height by mouse look
            targetY = posY + riderLook.yCoord * targetDist;
        } else {
            // control height by jumping/sneaking
            if (rider.movementInput.jump || rider.movementInput.sneak) {
                if (!isSaddled()) {
                    // set target directly in front of the dragon
                    targetX = posX + dragonLook.xCoord * targetDist;
                    targetZ = posZ + dragonLook.zCoord * targetDist;
                }
                
                targetY = posY;

                if (rider.movementInput.jump) {
                    targetY += targetDist;
                } else if (rider.movementInput.sneak) {
                    targetY -= targetDist;
                }
            }
        }
        
        // control speed with forward/backward keys
        if (rider.movementInput.moveForward < 0) {
            moveSpeed -= 0.05;
        } else if (rider.movementInput.moveForward > 0) {
            moveSpeed += 0.05;
        }

        // limit movement speed
        moveSpeed = MathF.clamp(moveSpeed, 0, maxMoveSpeed);

        if (onGround) {
            // lift-off by jumping
            if (rider.movementInput.jump) {
                onGround = false;
                motionX = riderLook.xCoord * 0.5;
                motionY = 0.5;
                motionZ = riderLook.zCoord * 0.5;
            }

            // move on ground
//            if (player.movementInput.moveForward != 0 || player.movementInput.moveStrafe != 0) {
//                moveForward = -player.movementInput.moveForward * 0.75f;
//                moveStrafing = -player.movementInput.moveStrafe * 0.75f;
//            }
        }
    }
    
    @Override
    public void setRandomTarget() {
        forceNewTarget = false;
        
        // get new random pos within 64 units
        targetZ += (0.5 - rand.nextDouble()) * 64;
        targetX += (0.5 - rand.nextDouble()) * 64;

        // fall slowly when unmounted
        if (riddenByEntity == null) {
            targetY -= rand.nextDouble() * 16;
        }
        
        L.log(Level.FINE, "Dragon {0} flight target automatically set to [{1} {2} {3}]", new Object[]{entityId, targetX, targetY, targetZ});
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (!worldObj.multiplayerWorld) {
            // consume saddle when being saddled
            if (!isSaddled() && GameUtils.consumePlayerEquippedItem(player, Item.saddle)) {
                L.log(Level.FINE, "Dragon {0} consumed a saddle", entityId);
                setSaddled(true);
                return false;
            }

            // consume food for healing
            if (getEntityHealth() != getMaxHealth()) {
                if (GameUtils.consumePlayerEquippedItem(player,
                        Item.fishRaw, Item.porkRaw, Item.beefRaw, Item.chickenRaw)) {
                    heal(8);
                    L.log(Level.FINE, "Dragon {0} consumed food", entityId);
                    return false;
                }
                
                if (GameUtils.consumePlayerEquippedItem(player, Item.rottenFlesh)) {
                    heal(4);
                    L.log(Level.FINE, "Dragon {0} consumed bad food", entityId);
                    return false;
                }
            }
            
            // consume glistering melon for temporary noclip mode
            if (isSaddled() && GameUtils.consumePlayerEquippedItem(player, Item.speckledMelon)) {
                worldObj.playAuxSFXAtEntity(null, 1004, (int)posX, (int)posY, (int)posZ, 0);
                noClipTicks += 200;
                L.log(Level.FINE, "Dragon {0} consumed a glistering melon", entityId);
                return false;
            }
            
            if (isSaddled() && GameUtils.consumePlayerEquippedItem(player, Item.blazePowder)) {
                worldObj.playAuxSFXAtEntity(null, 1009, (int)posX, (int)posY, (int)posZ, 0);
                flameBreathTicks += 100;
                L.log(Level.FINE, "Dragon {0} consumed blaze powder", entityId);
                return false;
            }
            
            // allow mounting on ground only
            if (onGround) {
                L.log(Level.FINE, "Dragon {0} got a mount request from {1}", new Object[]{entityId, player.username});
                setRider(player);
                return false;
            }

            // transform to egg when in air and being ridden by the user
            if (riddenByEntity == player) {
                L.log(Level.FINE, "Dragon {0} got a transform request from {1}", new Object[]{entityId, player.username});
                transformToEgg(player);
                return false;
            } 
        }
        
        return super.interact(player);
    }

    @Override
    public void heal(int health) {
        for (int i = 0; i < health; i++) {
            spawnBodyParticle("heart");
        }
        
        L.log(Level.FINE, "Dragon {0} got healed by {1}", new Object[]{entityId, health});
        
        super.heal(health);
    }
    
    public void appear() {
        spawnExplosionParticle();
        worldObj.playSoundAtEntity(this, "mob.endermen.portal", 1, 0.5f + (0.5f - rand.nextFloat()) * 0.1f);
    }
    
    private void transformToEgg(EntityPlayer player) {
        if (health <= 0) {
            // no can do
            return;
        }
        
        appear();

        double px = Math.round(posX) + 0.5;
        double py = Math.round(posY) + 0.5;
        double pz = Math.round(posZ) + 0.5;

        DragonEgg egg = new DragonEgg(worldObj, px, py, pz);
        egg.motionX = motionX;
        egg.motionZ = motionZ;
        worldObj.spawnEntityInWorld(egg);
        
        // drop equipped sattle
        if (isSaddled()) {
            if (player == null) {
                dropItem(Item.saddle.shiftedIndex, 1);
            } else {
                ItemStack saddle = new ItemStack(Item.saddle, 1);
                if (!player.inventory.addItemStackToInventory(saddle)) {
                    dropItem(Item.saddle.shiftedIndex, 1);
                }
            }
        }
        
        if (player != null) {
            player.mountEntity(egg);
        }
        
        setEntityDead();
        
        L.log(Level.FINE, "Dragon {0} transformed to egg", entityId);
    }
    
    @Override
    public boolean showOverlay() {
        return noClip && !isDead;
    }
    
    @Override
    public double getMountedYOffset() {
        return yMountedOffset * getBodySize();
    }

    public int getControlMode() {
        return controlMode;
    }

    public void setControlMode(int controlMode) {
        this.controlMode = controlMode;
    }
}
