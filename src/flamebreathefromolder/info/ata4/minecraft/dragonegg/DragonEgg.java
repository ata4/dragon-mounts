/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragonegg;

import info.ata4.minecraft.dragon.RidableVolantDragon;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.*;

/**
 * A dragon egg that is affected by gravity and can spawn dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonEgg extends EntityFallingSand {
    
    private static final Logger L = Logger.getLogger(mod_DragonMounts.class.getName());
    
    private boolean launched;
    private RidableVolantDragon spawnDragon = null;
    
    public DragonEgg(World world) {
        super(world);
    }

    public DragonEgg(World world, double x, double y, double z) {
        super(world, x, y, z, Block.dragonEgg.blockID);
        fallTime = 1;
        preventEntitySpawning = false;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        
        // don't convert into item when flying long distances
        if (fallTime >= 100) {
            fallTime = 99;
        }
        
        if (riddenByEntity != null && riddenByEntity instanceof EntityPlayer) {
            fallDistance = 0;
            
            // spawn and mount dragon when on highest point and not blocked
            if (launched && motionY < 0) {
                if (spawnDragon == null) {
                    spawnDragon = new RidableVolantDragon(worldObj);
                }
                
                float yaw = riddenByEntity.rotationYaw;
                
                spawnDragon.setPosition(posX, posY, posZ);
                
                // exclude player from spawn prevention checking
                riddenByEntity.preventEntitySpawning = false;
                
                if (spawnDragon.getCanSpawnHere()) {
                    worldObj.spawnEntityInWorld(spawnDragon);
                    
                    spawnDragon.rotationYaw = yaw + 180;
                    spawnDragon.appear();
                    spawnDragon.setSaddled(true);
                    spawnDragon.setRider((EntityPlayer) riddenByEntity);
                    spawnDragon = null;
                    
                    L.log(Level.FINE, "Dragon egg {0} spawned a dragon at [{1} {2} {3}]", new Object[]{entityId, posX, posY, posZ});

                    setEntityDead();
                }
                
                if (riddenByEntity != null) {
                    riddenByEntity.preventEntitySpawning = true;
                }
            }
        }
        
        if (handleWaterMovement()) {
            inWater = true;
            motionY *= 0.5;
        } else {
            inWater = false;
        }
        
        if (!launched) {
            motionX *= 0.9;
            motionY *= 0.9;
            motionZ *= 0.9;

            if (!inWater) {
                for (int l = 0; l < 2; l++) {
                    float nx = (rand.nextFloat() - 0.5f) * 0.25f;
                    float ny = -0.5f + (rand.nextFloat() - 0.5f) * 0.25f;
                    float nz = (rand.nextFloat() - 0.5f) * 0.25f;

                    worldObj.spawnParticle("cloud", posX, posY, posZ, nx, ny, nz);
                }
            }
        }
        
        float nx = (rand.nextFloat() - 0.5f);
        float ny = (rand.nextFloat() - 0.5f);
        float nz = (rand.nextFloat() - 0.5f);

        worldObj.spawnParticle("portal", posX, posY, posZ, nx, ny, nz);
        
        if (!isEntityAlive() && spawnDragon != null) {
            L.log(Level.FINE, "Dragon egg {0} couldn''t spawn a dragon!", entityId);
        }
    }

    public void launch() {
        launched = true;
        motionY = 1;
    }
    
    @Override
    public double getMountedYOffset() {
        return height * 0.45;
    }
    
    @Override
    public boolean interact(EntityPlayer player) {
        if (riddenByEntity == player) {
            player.mountEntity(this);
            return true;
        }
        
        return false;
    }
}
