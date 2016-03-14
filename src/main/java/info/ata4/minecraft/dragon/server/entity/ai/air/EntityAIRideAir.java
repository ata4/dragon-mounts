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
        double dist = 0.1;
        if (isFlyUp()) {
            dist = 10;
        }
        
        Vec3 dir = rider.getLookVec();

        double x = dragon.posX + dir.xCoord * dist;
        double y = dragon.posY + dir.yCoord * dist;
        double z = dragon.posZ + dir.zCoord * dist;

        dragon.getMoveHelper().setMoveTo(x, y, z, 1);
    }
}
