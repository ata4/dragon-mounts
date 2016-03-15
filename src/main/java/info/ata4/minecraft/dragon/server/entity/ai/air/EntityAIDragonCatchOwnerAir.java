/*
 ** 2013 November 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.air;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAIDragonCatchOwner;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonCatchOwnerAir extends EntityAIDragonCatchOwner {

    public EntityAIDragonCatchOwnerAir(EntityTameableDragon dragon) {
        super(dragon);
    }

    @Override
    public void updateTask() {
//        DragonFlightWaypoint wp = dragon.getWaypoint();
//        wp.setEntity(owner);
//        
//        double dist = wp.getDistance();
//        double yOfs = MathX.clamp(dist, 0, 64);
//        
//        wp.posY -= (int) yOfs;
//        
//        if (wp.isNear()) {
//            owner.mountEntity(dragon);
//        }
//        
//        dragon.setMoveSpeedAirHoriz(1);
    }
}
