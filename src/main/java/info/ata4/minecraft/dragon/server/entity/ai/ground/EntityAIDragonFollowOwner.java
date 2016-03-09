/*
 ** 2013 November 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.ground;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.ai.EntityAIFollowOwner;

/**
 * Modified EntityAIFollowOwner that won't run if the pet is sitting.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonFollowOwner extends EntityAIFollowOwner {

    private final EntityTameableDragon dragon;

    public EntityAIDragonFollowOwner(EntityTameableDragon dragon, double followSpeedIn, float minDistIn, float maxDistIn) {
        super(dragon, followSpeedIn, minDistIn, maxDistIn);
        this.dragon = dragon;
    }
    
    /**
     * Updates the task
     */
    @Override
    public void updateTask() {        
        // don't move when sitting
        if (dragon.isSitting()) {
            return;
        }
          
        super.updateTask();
    }
}
