/*
 ** 2012 April 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.ground;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.AxisAlignedBB;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIWatchLiving extends EntityAIBase {

    private EntityTameableDragon dragon;
    private Entity watchedEntity;
    private float maxDist;
    private int watchTicks;
    private float watchChance;

    public EntityAIWatchLiving(EntityTameableDragon dragon, float maxDist, float watchChance) {
        this.dragon = dragon;
        this.maxDist = maxDist;
        this.watchChance = watchChance;
        setMutexBits(2);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (dragon.getRNG().nextFloat() >= watchChance) {
            return false;
        }
        
        watchedEntity = null;
        
        if (watchedEntity == null) {
            AxisAlignedBB aabb = dragon.getEntityBoundingBox().expand(maxDist, dragon.height, maxDist);
            Class clazz = EntityLiving.class;
            watchedEntity = dragon.worldObj.findNearestEntityWithinAABB(clazz, aabb, dragon);
        }

        if (watchedEntity != null) {
            // don't try to look at the rider when being ridden
            if (watchedEntity == dragon.getRidingPlayer()) {
                watchedEntity = null;
            }
            
            // watch the owner a little longer
            if (watchedEntity == dragon.getOwner()) {
                watchTicks *= 3;
            }
        }

        return watchedEntity != null;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting() {
        if (!watchedEntity.isEntityAlive()) {
            return false;
        }

        if (dragon.getDistanceSqToEntity(watchedEntity) > maxDist * maxDist) {
            return false;
        } else {
            return watchTicks > 0;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        watchTicks = 40 + dragon.getRNG().nextInt(40);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        dragon.renderYawOffset = 0;
        watchedEntity = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask() {
        double lx = watchedEntity.posX;
        double ly = watchedEntity.posY + watchedEntity.getEyeHeight();
        double lz = watchedEntity.posZ;
        dragon.getLookHelper().setLookPosition(lx, ly, lz, dragon.getHeadYawSpeed(), dragon.getHeadPitchSpeed());
        watchTicks--;
    }
}
