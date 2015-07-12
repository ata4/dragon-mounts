/*
 ** 2012 April 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */

package info.ata4.minecraft.dragon.server.entity.ai.ground;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.network.DragonOrbTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;


public class EntityAIRangedBreathAttack extends EntityAIBase {
  /** The entity the AI instance has been applied to */
  private final EntityTameableDragon dragon;
  private EntityLivingBase attackTarget;
  /**
   * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
   * attackDelayAtMaxRange.
   */
  private double entityMoveSpeed;
  private int canSeeTargetTickCount;

  private float minAttackDistanceSQ;
  private float optimalAttackDistanceSQ;
  private float maxAttackDistanceSQ;

  private int targetDeselectedCountDown = 0;  // Countdown after player deselects target
  private DragonOrbTarget currentTarget = null;

  public EntityAIRangedBreathAttack(EntityTameableDragon i_dragon, double i_entityMoveSpeed,
                                    float i_minAttackDistance, float i_optimalAttackDistance, float i_maxAttackDistance)
  {
    this.dragon = i_dragon;
    this.entityMoveSpeed = i_entityMoveSpeed;
    this.minAttackDistanceSQ = i_minAttackDistance * i_minAttackDistance;
    this.optimalAttackDistanceSQ = i_optimalAttackDistance * i_optimalAttackDistance;
    this.maxAttackDistanceSQ = i_maxAttackDistance * i_maxAttackDistance;
    this.setMutexBits(3);
  }

  /**
   * Returns whether the EntityAIBase should begin execution.
   */
  public boolean shouldExecute()
  {
    DragonOrbTarget playerSelectedTarget = this.dragon.getBreathHelper().getPlayerSelectedTarget();
    return playerSelectedTarget != null || currentTarget != null;
  }

  /**
   * Returns whether an in-progress EntityAIBase should continue executing
   */
  public boolean continueExecuting()
  {
    return this.shouldExecute() || !this.dragon.getNavigator().noPath();
  }

  /**
   * Resets the task
   */
  public void resetTask()
  {
    this.attackTarget = null;
    this.canSeeTargetTickCount = 0;
    currentTarget = null;
    dragon.getBreathHelper().setBreathingTarget(null);
  }

  /**
   * Updates the task
   */
  public void updateTask()
  {
    // check which target the player has selected; if deselected, wait a short while before losing interest
    final int TARGET_DESELECTION_TIME = 60; // 60 ticks until dragon loses interest in target
    DragonOrbTarget playerSelectedTarget = this.dragon.getBreathHelper().getPlayerSelectedTarget();
    if (playerSelectedTarget != null) {
      currentTarget = playerSelectedTarget;
      targetDeselectedCountDown = TARGET_DESELECTION_TIME;
    } else {
      if (targetDeselectedCountDown <= 0) {
        currentTarget = null;
      } else {
        --targetDeselectedCountDown;
      }
    }

    if (currentTarget == null) return;

    // check if target visible
    boolean canSeeTarget = true;
    if (currentTarget.getTypeOfTarget() == DragonOrbTarget.TypeOfTarget.ENTITY) {
      Entity targetEntity = currentTarget.getTargetEntity(dragon.worldObj);
      canSeeTarget = (targetEntity == null) ? false : dragon.getEntitySenses().canSee(targetEntity);
    }
    if (canSeeTarget) {
      ++this.canSeeTargetTickCount;
      currentTarget.setEntityLook(dragon.worldObj, dragon.getLookHelper(),
                                  dragon.getHeadYawSpeed(), dragon.getHeadPitchSpeed());
    } else {
      this.canSeeTargetTickCount = 0;
    }

    // navigate to appropriate range
    final int SEE_TARGET_TICK_THRESHOLD = 40;
    double distanceToTargetSQ = currentTarget.distanceSQtoTarget(dragon.worldObj, dragon.getPositionVector());
    if (distanceToTargetSQ < 0) {
      // do nothing since distance not meaningful
    } else if (distanceToTargetSQ <= minAttackDistanceSQ) {
      //todo this will bite instead of breathe, or alternatively back up?
    } else {
      if ((distanceToTargetSQ <= maxAttackDistanceSQ && this.canSeeTargetTickCount >= SEE_TARGET_TICK_THRESHOLD)
          || distanceToTargetSQ <= optimalAttackDistanceSQ) {
        dragon.getNavigator().clearPathEntity();  // stop moving
      } else {
        currentTarget.setNavigationPath(dragon.worldObj, dragon.getNavigator(), entityMoveSpeed);
      }
    }

    if (distanceToTargetSQ <= maxAttackDistanceSQ && canSeeTarget) {
      dragon.getBreathHelper().setBreathingTarget(currentTarget);
    } else {
      dragon.getBreathHelper().setBreathingTarget(null);
    }


  }
//    private EntityTameableDragon dragon;
//    private Entity watchedEntity;
//    private float maxDist;
//    private int watchTicks;
//    private float watchChance;
//
//    public EntityAIRangedBreathAttack(EntityTameableDragon dragon, float maxDist, float watchChance) {
//        this.dragon = dragon;
//
//        setMutexBits(2);
//    }
//
//    /**
//     * Returns whether the EntityAIBase should begin execution.
//     */
//    @Override
//    public boolean shouldExecute() {
//        if (dragon.getRNG().nextFloat() >= watchChance) {
//            return false;
//        }
//
//        watchedEntity = null;
//
//        if (watchedEntity == null) {
//            AxisAlignedBB aabb = dragon.getEntityBoundingBox().expand(maxDist, dragon.height, maxDist);
//            Class clazz = EntityLiving.class;
//            watchedEntity = dragon.worldObj.findNearestEntityWithinAABB(clazz, aabb, dragon);
//        }
//
//        if (watchedEntity != null) {
//            // don't try to look at the rider when being ridden
//            if (watchedEntity == dragon.getRidingPlayer()) {
//                watchedEntity = null;
//            }
//
//            // watch the owner a little longer
//            if (watchedEntity == dragon.getOwner()) {
//                watchTicks *= 3;
//            }
//        }
//
//        return watchedEntity != null;
//    }
//
//    /**
//     * Returns whether an in-progress EntityAIBase should continue executing
//     */
//    @Override
//    public boolean continueExecuting() {
//        if (!watchedEntity.isEntityAlive()) {
//            return false;
//        }
//
//        if (dragon.getDistanceSqToEntity(watchedEntity) > maxDist * maxDist) {
//            return false;
//        } else {
//            return watchTicks > 0;
//        }
//    }
//
//    /**
//     * Execute a one shot task or start executing a continuous task
//     */
//    @Override
//    public void startExecuting() {
//        watchTicks = 40 + dragon.getRNG().nextInt(40);
//    }
//
//    /**
//     * Resets the task
//     */
//    @Override
//    public void resetTask() {
//        dragon.renderYawOffset = 0;
//        watchedEntity = null;
//    }
//
//    /**
//     * Updates the task
//     */
//    @Override
//    public void updateTask() {
//        double lx = watchedEntity.posX;
//        double ly = watchedEntity.posY + watchedEntity.getEyeHeight();
//        double lz = watchedEntity.posZ;
//        dragon.getLookHelper().setLookPosition(lx, ly, lz, 10, dragon.getVerticalFaceSpeed());
//        watchTicks--;
//    }

}
