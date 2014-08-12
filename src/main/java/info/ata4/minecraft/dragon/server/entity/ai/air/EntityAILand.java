/*
 ** 2013 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.air;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.Vec3;

/**
 * Dragon AI for instant landing, if left unmounted in air.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAILand extends EntityAIBase {
    
    private final EntityTameableDragon dragon;
    private Vec3 landTarget;
    
    public EntityAILand(EntityTameableDragon dragon) {
        this.dragon = dragon;
    }

    @Override
    public boolean shouldExecute() {
        if (!dragon.isFlying() || dragon.getRidingPlayer() != null) {
            return false;
        }
        
        landTarget = RandomPositionGenerator.findRandomTarget(dragon, 16, 256);
        
        return landTarget != null;
    }

    @Override
    public void startExecuting() {
        landTarget.yCoord = 0;
        dragon.getWaypoint().setVector(landTarget);
        dragon.setMoveSpeedAirHoriz(1);
        dragon.setMoveSpeedAirVert(0);
    }
}
