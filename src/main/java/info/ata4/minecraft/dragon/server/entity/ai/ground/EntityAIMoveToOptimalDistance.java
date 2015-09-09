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
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**Moves the dragon to the optimal distance from the breath weapon target- not too close, not too far
 * The location to move to taken from the dragon breath helper's getBreathTargetForMoving
 *   (The location is set by the targeting AI)
 */
public class EntityAIMoveToOptimalDistance extends EntityAIBase {
  /** The entity the AI instance has been applied to */
  private final EntityTameableDragon dragon;
  private double entityMoveSpeed;
  private int canSeeTargetTickCount;

  private float minAttackDistanceSQ;
  private float optimalAttackDistanceSQ;
  private float maxAttackDistanceSQ;

  public EntityAIMoveToOptimalDistance(EntityTameableDragon i_dragon, double i_entityMoveSpeed,
                                       float i_minAttackDistance, float i_optimalAttackDistance, float i_maxAttackDistance)
  {
    this.dragon = i_dragon;
    this.entityMoveSpeed = i_entityMoveSpeed;
    this.minAttackDistanceSQ = i_minAttackDistance * i_minAttackDistance;
    this.optimalAttackDistanceSQ = i_optimalAttackDistance * i_optimalAttackDistance;
    this.maxAttackDistanceSQ = i_maxAttackDistance * i_maxAttackDistance;
    this.setMutexBits(3);
    canSeeTargetTickCount = 0;
  }

  /**
   * Returns whether the EntityAIBase should begin execution.
   */
  public boolean shouldExecute()
  {
    return dragon.getBreathHelper().hasBreathTargetForMoving() && !dragon.isRiding();
  }

  /**
   * Returns whether an in-progress EntityAIBase should continue executing
   */
  public boolean continueExecuting()
  {
    return this.shouldExecute();
  }

  /**
   * Resets the task
   */
  public void resetTask()
  {
    dragon.getNavigator().clearPathEntity();  // stop moving
  }

  private BreathWeaponTarget lastTickTarget = null;

  /**
   * Updates the task:
   * moves towards (or away from) the breath target
   * looks at the target.
   */
  public void updateTask()
  {
    BreathWeaponTarget currentTarget = dragon.getBreathHelper().getBreathTargetForMoving();
    boolean targetChanged = !BreathWeaponTarget.approximatelyMatches(currentTarget, lastTickTarget);
    lastTickTarget = currentTarget;

    if (currentTarget == null) {
      dragon.getNavigator().clearPathEntity();  // stop moving
      return;
    }

    // check if target visible: if so, look at it
    boolean canSeeTarget = true;
    if (currentTarget.getTypeOfTarget() == BreathWeaponTarget.TypeOfTarget.ENTITY) {
      Entity targetEntity = currentTarget.getTargetEntity(dragon.worldObj);
      canSeeTarget = (targetEntity != null) && dragon.getEntitySenses().canSee(targetEntity);
    }
    if (canSeeTarget) {
      ++this.canSeeTargetTickCount;
      Vec3 dragonEyePos = dragon.getPositionVector().addVector(0, dragon.getEyeHeight(), 0);
      currentTarget.setEntityLook(dragon.worldObj, dragon.getLookHelper(), dragonEyePos,
                                  dragon.getHeadYawSpeed(), dragon.getHeadPitchSpeed());
    } else {
      this.canSeeTargetTickCount = 0;
    }

    double distanceToTargetSQ = currentTarget.distanceSQtoTarget(dragon.worldObj, dragon.getPositionVector());

    // navigate to appropriate range: only change navigation path if the target has changed or
    //   if there is no navigation path currently in progress
    final int SEE_TARGET_TICK_THRESHOLD = 40;
    if (distanceToTargetSQ < 0) {
      // don't move since distance not meaningful - this is direction only
    } else if (distanceToTargetSQ <= minAttackDistanceSQ) {
      // back up to at least minimum range.
      PathNavigate pathNavigate = dragon.getNavigator();
      if (targetChanged || pathNavigate.noPath()) {
        currentTarget.setNavigationPathAvoid(dragon.worldObj, pathNavigate,
                dragon.getPositionVector().addVector(0, dragon.getEyeHeight(), 0),
                entityMoveSpeed,
                MathHelper.sqrt_double(optimalAttackDistanceSQ) + 1.0);
      }
    } else if (distanceToTargetSQ <= optimalAttackDistanceSQ) {
      dragon.getNavigator().clearPathEntity();  // at optimal distance - stop moving
    } else if (distanceToTargetSQ <= maxAttackDistanceSQ && this.canSeeTargetTickCount >= SEE_TARGET_TICK_THRESHOLD) {
      dragon.getNavigator().clearPathEntity();  // have been within range to attack for a while - stop moving
    } else {
      PathNavigate pathNavigate = dragon.getNavigator(); // still too far! move closer
      if (targetChanged || pathNavigate.noPath()) {
        currentTarget.setNavigationPath(dragon.worldObj, dragon.getNavigator(), entityMoveSpeed);
      }
    }

  }
}
