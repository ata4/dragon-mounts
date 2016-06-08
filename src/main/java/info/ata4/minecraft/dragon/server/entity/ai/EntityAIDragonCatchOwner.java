/*
 ** 2013 November 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonCatchOwner extends EntityAIDragonBase {
    
    protected EntityPlayer owner;
    
    public EntityAIDragonCatchOwner(EntityTameableDragon dragon) {
        super(dragon);
    }

    @Override
    public boolean shouldExecute() {        
        // don't catch if leashed
        if (dragon.getLeashed()) {
            return false;
        }
        
        owner = (EntityPlayer) dragon.getOwner();
        
        // don't catch if ownerless 
        if (owner == null) {
            return false;
        }
        
        // no point in catching players in creative mode
        if (owner.capabilities.isCreativeMode) {
            return false;
        }
        
        // don't catch if already being ridden
        if (dragon.isPassenger(owner)) {
            return false;
        }
        
        // don't catch if owner has a working Elytra equipped
        // note: isBroken() is misleading, it actually checks if the items is usable
        ItemStack itemStack = owner.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (itemStack != null && itemStack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemStack)) {
            return false;
        }
        
        // don't catch if owner is too far away
        double followRange = getFollowRange();
        if (dragon.getDistanceToEntity(owner) > followRange) {
            return false;
        }
                
        return owner.fallDistance > 4;
    }

    @Override
    public boolean continueExecuting() {
        return shouldExecute() && !dragon.getNavigator().noPath();
    }
    
    @Override
    public void updateTask() {
        // catch owner in flight if possible
        if (!dragon.isFlying()) {
            dragon.liftOff();
        }
        
        // mount owner if close enough, otherwise move to owner
        if (dragon.getDistanceToEntity(owner) < dragon.width) {
            owner.startRiding(dragon);
        } else {
            dragon.getNavigator().tryMoveToEntityLiving(owner, 1);
        }
    }
}
