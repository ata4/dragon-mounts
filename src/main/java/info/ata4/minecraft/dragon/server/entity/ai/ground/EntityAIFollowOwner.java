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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Modified EntityAIFollowOwner that won't run if the pet is sitting.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIFollowOwner extends EntityAIBase {

    private EntityTameableDragon dragon;
    private EntityLivingBase owner;
    private World world;
    private double speed;
    private PathNavigate nav;
    private int updateTicks;
    private float maxDist;
    private float minDist;
    private boolean avoidWater;

    public EntityAIFollowOwner(EntityTameableDragon dragon, double speed, float minDist, float maxDist) {
        this.dragon = dragon;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        
        nav = dragon.getNavigator();
        world = dragon.worldObj;
        
        setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        EntityLivingBase ownerCurrent = dragon.getOwner();

        if (ownerCurrent == null) {
            return false;
        }
        
        if (dragon.isSitting()) {
            return false;
        }
        
        if (dragon.getDistanceSqToEntity(ownerCurrent) < minDist * minDist) {
            return false;
        }
        
        owner = ownerCurrent;
        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting() {
        if (nav.noPath()) {
            return false;
        }
        
        if (dragon.isSitting()) {
            return false;
        }
        
        return true;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        updateTicks = 0;
        avoidWater = dragon.getNavigator().getAvoidsWater();
        dragon.getNavigator().setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        owner = null;
        nav.clearPathEntity();
        dragon.getNavigator().setAvoidsWater(avoidWater);
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
        
        // face towards owner
        dragon.getLookHelper().setLookPositionWithEntity(owner, 10, (float) dragon.getVerticalFaceSpeed());

        // update every 10 ticks only from here
        if (--updateTicks > 0) {
            return;
        }
        updateTicks = 10;
        
        // finish task if it can move to the owner
        if (nav.tryMoveToEntityLiving(owner, speed)) {
            return;
        }
        
        // move only but don't teleport if leashed
        if (dragon.getLeashed()) {
            return;
        }
        
        // teleport only the owner is far enough
        if (dragon.getDistanceSqToEntity(owner) < maxDist * maxDist) {
            return;
        }
        
        // teleport dragon near owner
        int minX = MathHelper.floor_double(owner.posX) - 2;
        int minY = MathHelper.floor_double(owner.posZ) - 2;
        int minZ = MathHelper.floor_double(owner.boundingBox.minY);

        for (int bx = 0; bx <= 4; ++bx) {
            for (int by = 0; by <= 4; ++by) {
                if ((bx < 1 || by < 1 || bx > 3 || by > 3) &&
                        World.doesBlockHaveSolidTopSurface(world, minX + bx, minZ - 1, minY + by) &&
                        !world.getBlock(minX + bx, minZ, minY + by).isNormalCube() &&
                        !world.getBlock(minX + bx, minZ + 1, minY + by).isNormalCube()) {
                    dragon.setLocationAndAngles(minX + bx + 0.5, minZ, minY + by + 0.5,
                            dragon.rotationYaw, dragon.rotationPitch);
                    nav.clearPathEntity();
                    return;
                }
            }
        }
    }
}
