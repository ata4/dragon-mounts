/*
** 2016 April 26
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.air;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAIDragonBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonFollowOwner extends EntityAIDragonBase {
    
    protected EntityPlayer owner;

    public EntityAIDragonFollowOwner(EntityTameableDragon dragon) {
        super(dragon);
    }

    @Override
    public boolean shouldExecute() {
        if (!dragon.isFlying()) {
            return false;
        }
        
        owner = (EntityPlayer) dragon.getOwner();
        
        // don't follow if ownerless 
        if (owner == null) {
            return false;
        }
        
        // don't follow if already being ridden
        if (dragon.isPassenger(owner)) {
            return false;
        }
        
        // follow only if the ower is using an Elytra
        return owner.isElytraFlying();
    }
    
    @Override
    public void updateTask() {
        dragon.getNavigator().tryMoveToEntityLiving(owner, 1);
    }
}
