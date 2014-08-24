/*
 ** 2012 August 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity;

import cpw.mods.fml.relauncher.ReflectionHelper;
import info.ata4.minecraft.dragon.server.entity.ai.DragonFlightWaypoint;
import info.ata4.minecraft.dragon.util.reflection.PrivateFields;
import info.ata4.minecraft.dragon.util.math.MathX;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class EntityFlyingTameable extends EntityTameable {
    
    private static final Logger L = LogManager.getLogger();
    
    private static final int IN_AIR_THRESH = 10;
    
    public static final IAttribute MOVE_SPEED_AIR = new RangedAttribute("generic.movementSpeedAir", 1.5, 0.0, Double.MAX_VALUE).setDescription("Movement Speed Air").setShouldWatch(true);
    
    // data value IDs
    private static final int INDEX_FLYING = 18;
    private static final int INDEX_CAN_FLY = 19;
    
    // data NBT IDs
    private static String NBT_FLYING = "Flying";
    private static String NBT_CAN_FLY = "CanFly";
       
    public EntityAITasks airTasks;
    
    private DragonFlightWaypoint waypoint;
    private double airSpeedHorizonal = 1.5;
    private double airSpeedVertical = 0;
    private float yawAdd;
    private int yawSpeed = 30;
    private int inAirTicks;
   
    public EntityFlyingTameable(World world) {
        super(world);
        waypoint = new DragonFlightWaypoint(this);
        airTasks = new EntityAITasks(world != null ? world.theProfiler : null);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(INDEX_FLYING, Byte.valueOf((byte) 0));
        dataWatcher.addObject(INDEX_CAN_FLY, Byte.valueOf((byte) 0));
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(MOVE_SPEED_AIR);
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean(NBT_FLYING, isFlying());
        nbt.setBoolean(NBT_CAN_FLY, isCanFly());
        waypoint.writeToNBT(nbt);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        setFlying(nbt.getBoolean(NBT_FLYING));
        setCanFly(nbt.getBoolean(NBT_CAN_FLY));
        waypoint.readFromNBT(nbt);
    }
    
    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    @Override
    protected void fall(float par1) {
        // ignore fall damage if the entity can fly
        if (!isCanFly()) {
            super.fall(par1);
        }
    }
    
    /**
     * Causes this entity to lift off.
     */
    public void liftOff() {
        L.trace("liftOff");
        if (isCanFly()) {
            jump();
            
            // stronger jump for an easier lift-off
            motionY += 0.5;
            inAirTicks += 20;
            
            // don't use old waypoint
            waypoint.clear();
        }
    }
    
    private void setTasksEnabled(EntityAITasks tasks, boolean flag) {
        // disable task by increasing the tick rate to ridiculous extends
        ReflectionHelper.setPrivateValue(EntityAITasks.class, tasks, flag ? 3 : Integer.MAX_VALUE, PrivateFields.ENTITYAITASKS_TICKRATE);
    }
    
    protected boolean isGroundAIEnabled() {
        return !isFlying();
    }

    protected boolean isAirAIEnabled() {
        return isFlying();
    }
    
    @Override
    protected void updateAITasks() {
        setTasksEnabled(tasks, isGroundAIEnabled());
        setTasksEnabled(airTasks, isAirAIEnabled());
        
        super.updateAITasks();
        airTasks.onUpdateTasks();
    }
    
    @Override
    public void onLivingUpdate() {
        // behave like a normal entity if we can't fly
        if (!isCanFly()) {
            if (isFlying()) {
                setFlying(false);
            }
            
            super.onLivingUpdate();
            return;
        }
        
        if (isServer()) {
            // delay flying state for 10 ticks (0.5s)
            if (!onGround) {
                inAirTicks++;
            } else {
                inAirTicks = 0;
            }
            
            setFlying(inAirTicks > IN_AIR_THRESH);
        }
        
        if (isFlying()) {
            if (isClient()) {
                onUpdateFlyingClient();
            } else {
                onUpdateFlyingServer();
            }
        } else {
            super.onLivingUpdate();
        }
    }
    
    private void onUpdateFlyingClient() {
        if (newPosRotationIncrements > 0) {
            double px = posX + (newPosX - posX) / newPosRotationIncrements;
            double py = posY + (newPosY - posY) / newPosRotationIncrements;
            double pz = posZ + (newPosZ - posZ) / newPosRotationIncrements;
            double newYaw = MathX.normDeg(newRotationYaw - rotationYaw);
            
            rotationYaw += (float) newYaw / newPosRotationIncrements;
            rotationPitch += ((float) newRotationPitch - rotationPitch) / newPosRotationIncrements;
            
            --newPosRotationIncrements;
            
            setPosition(px, py, pz);
            setRotation(rotationYaw, rotationPitch);
        }
    }
    
    private void onUpdateFlyingServer() {
        float friction = 0;
        
        // move towards waypoint if the distance is significant
        if (!waypoint.isNear()) {
            double deltaX = waypoint.posX - posX;
            double deltaY = waypoint.posY - posY;
            double deltaZ = waypoint.posZ - posZ;
            
            double speedAir = getEntityAttribute(MOVE_SPEED_AIR).getAttributeValue();
            double speedHoriz = airSpeedHorizonal * speedAir;
            double speedVert = airSpeedVertical;
            
            deltaY /= Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            deltaY = MathX.clamp(deltaY, -speedHoriz, speedHoriz) / 3;

            double motionHypot = Math.hypot(motionX, motionZ) + 1;
            
            double newYaw = Math.toDegrees((Math.PI * 2) - Math.atan2(deltaX, deltaZ));
            double yawDelta = MathX.normDeg(newYaw - rotationYaw);
            yawDelta = MathX.clamp(yawDelta, -yawSpeed, yawSpeed);

            yawAdd *= 0.8f;
            yawAdd += yawDelta * (0.7 / motionHypot);

            rotationYaw += yawAdd * 0.1f;

            // calculate acceleration
            Vec3 motionVec = Vec3.createVectorHelper(motionX, motionY, motionZ).normalize();
            Vec3 deltaVec = Vec3.createVectorHelper(deltaX, deltaY, deltaZ).normalize();
            Vec3 rotVec = Vec3.createVectorHelper(
                    -Math.sin(Math.toRadians(rotationYaw)),
                    motionY,
                    Math.cos(Math.toRadians(rotationYaw))
                ).normalize();
            
            float tmp1 = (float) (rotVec.dotProduct(deltaVec) + 0.5) / 1.5f;
            if (tmp1 < 0) {
                tmp1 = 0;
            }
            float tmp2 = (float) (2 / (motionHypot + 1));
            float acc = 0.06f * (tmp1 * tmp2 + (1 - tmp2));

            // set vertical motion
            motionY = deltaY + speedVert;

            // update motion in facing direction
            moveFlying(0, (float) speedHoriz, acc);
            
            friction = (float) (motionVec.dotProduct(rotVec) + 1) / 2f;
        }
        
        friction = 0.8f + 0.15f * friction;

        if (inWater) {
            friction *= 0.8f;
        }

        motionX *= friction;
        motionY *= friction;
        motionZ *= friction;

        // apply movement
        moveEntity(motionX, motionY, motionZ);
 
        // update AI
        if (isAIEnabled()) {
            worldObj.theProfiler.startSection("newAi");
            updateAITasks();
            worldObj.theProfiler.endSection();
        } else {
            worldObj.theProfiler.startSection("oldAi");
            updateEntityActionState();
            worldObj.theProfiler.endSection();
            rotationYawHead = rotationYaw;
        }

        // apply collision
        List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.2, 0, 0));
        if (entities != null && !entities.isEmpty()) {
            for (Entity entity : entities) {
                if (entity.canBePushed()) {
                    entity.applyEntityCollision(this);
                }
            }
        }
    }
    
    /**
     * Returns true if the entity is flying.
     */
    public boolean isFlying() {
        return (dataWatcher.getWatchableObjectByte(INDEX_FLYING) & 1) != 0;
    }

    /**
     * Set the flying flag of the entity.
     */
    public void setFlying(boolean flying) {
        dataWatcher.updateObject(INDEX_FLYING, Byte.valueOf(flying ? (byte) 1 : (byte) 0));
    }

    public boolean isCanFly() {
        return (dataWatcher.getWatchableObjectByte(INDEX_CAN_FLY) & 1) != 0;
    }

    public void setCanFly(boolean canFly) {
        L.trace("setCanFly({})", canFly);
        dataWatcher.updateObject(INDEX_CAN_FLY, Byte.valueOf(canFly ? (byte) 1 : (byte) 0));
    }
    
    /**
     * Returns the distance to the ground while the entity is flying.
     */
    public double getAltitude() {
        int blockX = (int) (posX - 0.5);
        int blockZ = (int) (posZ - 0.5);
        return posY - worldObj.getHeightValue(blockX, blockZ);
    }
    
    public DragonFlightWaypoint getWaypoint() {
        return waypoint;
    }
    
    /**
     * Returns relative speed multiplier for the horizontal speed.
     * 
     * @return relative horizontal speed multiplier
     */
    public double getMoveSpeedAirHoriz() {
        return this.airSpeedHorizonal;
    }
    
    /**
     * Sets new relative speed multiplier for the horizontal flying speed.
     * 
     * @param airSpeedHorizonal new relative horizontal speed multiplier
     */
    public void setMoveSpeedAirHoriz(double airSpeedHorizonal) {
        L.trace("setMoveSpeedAirHoriz({})", airSpeedHorizonal);
        this.airSpeedHorizonal = airSpeedHorizonal;
    }
    
    /**
     * Returns relative speed multiplier for the vertical flying speed.
     * 
     * @return relative vertical speed multiplier
     */
    public double getMoveSpeedAirVert() {
        return this.airSpeedVertical;
    }
    
    /**
     * Sets new relative speed multiplier for the vertical flying speed.
     * 
     * @param airSpeedHorizonal new relative vertical speed multiplier
     */
    public void setMoveSpeedAirVert(double airSpeedVertical) {
        L.trace("setMoveSpeedAirVert({})", airSpeedVertical);
        this.airSpeedVertical = airSpeedVertical;
    }
    
    /**
     * Checks if this entity is running on a client.
     * 
     * Required since MCP's isClientWorld returns the exact opposite...
     * 
     * @return true if the entity runs on a client or false if it runs on a server
     */
    public boolean isClient() {
        return worldObj.isRemote;
    }
    
    /**
     * Checks if this entity is running on a server.
     * 
     * @return true if the entity runs on a server or false if it runs on a client
     */
    public boolean isServer() {
        return !worldObj.isRemote;
    }
}
