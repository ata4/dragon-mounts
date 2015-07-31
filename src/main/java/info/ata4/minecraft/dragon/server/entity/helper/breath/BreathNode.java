package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * Created by TGG on 30/07/2015.
 */
public class BreathNode
{
  public BreathNode(Power i_power)
  {
    setPower(i_power);
  }

  public enum Power {SMALL, MEDIUM, LARGE}

  private float ageTicks;

  private float relativeSize = 1.0F;
  private float relativeLifetime = 1.0F;

//  public void setRelativeSize(double newSizeFactor)
//  {
//    relativeSize = (float)newSizeFactor;
//  }
//
//  public void setRelativeLifetime(double newLifetimeFactor)
//  {
//    relativeLifetime = (float)newLifetimeFactor;
//  }

  private static final double SPEED_VARIATION_ABS = 0.1;  // plus or minus this amount (3 std deviations)
  private static final double AGE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)
  private static final double SIZE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)

  public void randomiseProperties(Random rand) {
    relativeLifetime = (float)(MathX.getTruncatedGaussian(rand, 1, AGE_VARIATION_FACTOR));
    relativeSize = (float)(MathX.getTruncatedGaussian(rand, 1, SIZE_VARIATION_FACTOR));
  }

  public Vec3 getRandomisedStartingMotion(Vec3 initialDirection, Random rand)
  {
    float initialSpeed = getStartingSpeed();
    Vec3 direction = initialDirection.normalize();

    double actualMotionX = direction.xCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    double actualMotionY = direction.yCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    double actualMotionZ = direction.zCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    actualMotionX *= initialSpeed;
    actualMotionY *= initialSpeed;
    actualMotionZ *= initialSpeed;
    return new Vec3(actualMotionX, actualMotionY, actualMotionZ);
  }

  public float getStartingSpeed()
  {
    return speedPowerFactor * INITIAL_SPEED;
  }

  public float getMaxLifeTime()
  {
    return lifetimePowerFactor * relativeLifetime * DEFAULT_AGE_IN_TICKS;
  }

  public float getAgeTicks() {return ageTicks;}

//  /**
//   * moves the node for the indicated number of ticks
//   * splits the move into a number of steps.
//   * makes a record of all the blocks it touches, and the node location at each step
//   */
//  public void move(World world, Set<BlockPos> blocksTouched, Collection<Vec3> nodeLocations, int tickCount)
//  {
//    final float MAX_MOVE_DISTANCE_PER_STEP = 0.5F; // maximum number of blocks moved per step
//
//    float distanceToMove = tickCount * getStartingSpeed();
//    float numberOfStepsFrac = distanceToMove / MAX_MOVE_DISTANCE_PER_STEP;
//    int numberOfSteps = (int)numberOfStepsFrac + 1;
//    float stepDistance = distanceToMove / numberOfSteps;
//
//    Vec3 startPosition = position;
//    Vec3 endPosition = startPosition.addVector(direction.xCoord * stepDistance,
//                                                direction.yCoord * stepDistance,
//                                                direction.zCoord * stepDistance);
//    final boolean STOP_ON_LIQUID = true;
//    final boolean IGNORE_BOUNDING_BOX = false;
//    final boolean RETURN_NULL_IF_NO_COLLIDE = true;
//    MovingObjectPosition collidedBlock = world.rayTraceBlocks(startPosition, endPosition,
//            STOP_ON_LIQUID, IGNORE_BOUNDING_BOX,
//            !RETURN_NULL_IF_NO_COLLIDE);
//    if (collidedBlock == null) {
//      position = endPosition;
//    } else {
//
//    }
//
//    THIS ISN'T GOING TO WORK PROPERLY; USE ENTITY MOVEMENT INSTEAD'
//
//
//
//  }

  public boolean isDead()
  {
    return ageTicks > getMaxLifeTime();
  }

  public void updateAge(Entity parentEntity)
  {
    if (ageTicks++ > getMaxLifeTime()) {
      return;
    }
    // collision ages breath node faster
    if (parentEntity.isCollided) {
      ageTicks += 5;
    }

    // slow breath nodes age very fast (they look silly when sitting still)
    final double SPEED_THRESHOLD = getStartingSpeed() * 0.25;
    double speedSQ = parentEntity.motionX * parentEntity.motionX
                    + parentEntity.motionY * parentEntity.motionY
                    + parentEntity.motionZ * parentEntity.motionZ;
    if (speedSQ < SPEED_THRESHOLD * SPEED_THRESHOLD) {
      ageTicks += 20;
    }
  }

  // change size of entity AABB used for collisions
  // when the entity size is changed, it changes the bounding box but doesn't recentre it, so the xpos and zpos move
  //  (the entity update resetPositionToBB copies it back)
  // To fix this, we resize the AABB around the existing centre
  // it is ok to pass a partially constructed
  public void changeEntitySizeToMatch(Entity entity)
  {
    final float AABB_RELATIVE_TO_SIZE = 0.5F;  // how big is the AABB relative to the fireball size.

    float currentParticleSize = getCurrentSize();
    int width = (int)(currentParticleSize * AABB_RELATIVE_TO_SIZE);
    int height = (int)(currentParticleSize * AABB_RELATIVE_TO_SIZE);

    if (width != entity.width || height != entity.height) {
      AxisAlignedBB oldAABB = entity.getEntityBoundingBox();
      double oldMidptX = (oldAABB.minX + oldAABB.maxX)/2.0;
      double oldMidptZ = (oldAABB.minZ + oldAABB.maxZ)/2.0;

      entity.width = width;
      entity.height = height;

      AxisAlignedBB newAABB = new AxisAlignedBB(oldMidptX - width / 2.0, oldAABB.minY, oldMidptZ - width / 2.0,
              oldMidptX + width / 2.0, oldAABB.maxY, oldMidptZ + width / 2.0);
      entity.setEntityBoundingBox(newAABB);
    }
  }

  public float getCurrentSize() {
    final float YOUNG_AGE = 0.25F;
    final float OLD_AGE = 0.75F;
    float lifetimeFraction = getLifetimeFraction();

    float fractionOfFullSize = 1.0F;
    if (lifetimeFraction < YOUNG_AGE) {
      fractionOfFullSize = MathHelper.sin(lifetimeFraction / YOUNG_AGE * (float) Math.PI / 2.0F);
    }

    final float PARTICLE_MAX_SIZE = DEFAULT_BALL_SIZE * sizePowerFactor * relativeSize;
    final float INITIAL_SIZE = 0.2F * PARTICLE_MAX_SIZE;
    return INITIAL_SIZE + (PARTICLE_MAX_SIZE - INITIAL_SIZE) * MathHelper.clamp_float(fractionOfFullSize, 0.0F, 1.0F);
  }

  public float getLifetimeFraction() {
    float lifetimeFraction = (float)ageTicks / getMaxLifeTime();
    lifetimeFraction = MathHelper.clamp_float(lifetimeFraction, 0.0F, 1.0F);
    return lifetimeFraction;
  }

  private static final float INITIAL_SPEED = 1.2F; // blocks per tick at full speed
  private static final float DEFAULT_BALL_SIZE = 2.0F;
  private static final int DEFAULT_AGE_IN_TICKS = 40;

  private Power power;
  private float speedPowerFactor = 1.0F;
  private float lifetimePowerFactor = 1.0F;
  private float sizePowerFactor = 1.0F;

  private void setPower(Power newPower) {
    power = newPower;
    switch (newPower) {
      case SMALL: {
        speedPowerFactor = 0.25F;
        lifetimePowerFactor = 0.25F;
        sizePowerFactor = 0.25F;
        break;
      }
      case MEDIUM: {
        speedPowerFactor = 0.5F;
        lifetimePowerFactor = 0.5F;
        sizePowerFactor = 0.5F;
        break;
      }
      case LARGE: {
        speedPowerFactor = 1.0F;
        lifetimePowerFactor = 1.0F;
        sizePowerFactor = 1.0F;
        break;
      }

      default: {
        System.err.println("Invalid power in setPower:" + newPower);
      }
    }
  }


}
