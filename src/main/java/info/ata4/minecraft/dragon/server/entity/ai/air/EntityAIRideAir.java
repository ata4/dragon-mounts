/*
 ** 2012 April 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.air;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAIRide;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import net.minecraft.init.Items;
import net.minecraft.util.Vec3;

/**
 * AI for player-controlled air movements.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIRideAir extends EntityAIRide {
        
    public EntityAIRideAir(EntityTameableDragon dragon) {
        super(dragon);
    }
    
    @Override
    public void updateTask() {
        super.updateTask();
        
        double dist = 100;
        double x, y, z;
        
        // control with keyboard if carrot on a stick is equipped
        if (ItemUtils.hasEquipped(rider, Items.carrot_on_a_stick)) {
            Vec3 wp = rider.getLookVec();
            
            x = wp.xCoord;
//            y = wp.yCoord;
            z = wp.zCoord;
            
            // scale with distance
            x *= dist;
//            y *= dist;
            z *= dist;
            
            // convert to absolute position
            x += dragon.posX;
            z += dragon.posZ;

            y = dragon.posY;  // don't change altitude

            dragon.getWaypoint().set(x, y, z);
            
            dragon.setMoveSpeedAirHoriz(1);
            dragon.setMoveSpeedAirVert(0);
        } else {
            Vec3 wp = dragon.getLookVec();
            
            x = wp.xCoord;
//            y = wp.yCoord;
            z = wp.zCoord;

            // scale with distance
            x *= dist;
//            y *= dist;
            z *= dist;

            // convert to absolute position
            x += dragon.posX;
//            y += dragon.posY;
            z += dragon.posZ;

          y = dragon.posY;  // don't change altitude

          dragon.getWaypoint().set(x, y, z);
            
            double speedAir = 0; 
            
            // change speed with forward
            if (rider.moveForward != 0) {
                speedAir = 1;
                
                // fly slower backwards
                // (I'm surprised this is kinda working at all...)
                if (rider.moveForward < 0) {
                    speedAir *= 0.5;
                }

                speedAir *= rider.moveForward;
            }
            
            dragon.setMoveSpeedAirHoriz(speedAir);

            // control rotation with strafing
            if (rider.moveStrafing != 0) {
                dragon.rotationYaw -= rider.moveStrafing * 6;
            }

            // if we're breathing at a target, look at it
            BreathWeaponTarget breathWeaponTarget = dragon.getBreathHelper().getPlayerSelectedTarget();
            if (breathWeaponTarget != null) {
                Vec3 dragonEyePos = dragon.getPositionVector().addVector(0, dragon.getEyeHeight(), 0);
                breathWeaponTarget.setEntityLook(dragon.worldObj, dragon.getLookHelper(), dragonEyePos,
                        dragon.getHeadYawSpeed(), dragon.getHeadPitchSpeed());
            }

            double verticalSpeed = 0;

            // control height with custom keys
            if (isFlyUp()) {
                verticalSpeed = 0.5f;
            } else if (isFlyDown()) {
                verticalSpeed = -0.5f;
            }
            
            dragon.setMoveSpeedAirVert(verticalSpeed);
        }
    }
}
