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
 * BreathNode represents the age, size, and initial speed of each node;
 * It is used with an associated Entity:
 *   BreathNode tracks the age and size
 *   Entity tracks the position, motion, and collision detection
 *
 *   updateAge() and changeEntitySizeToMatch() are used to keep the two synchronised
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

  private static final double SPEED_VARIATION_ABS = 0.1;  // plus or minus this amount (3 std deviations)
  private static final double AGE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)
  private static final double SIZE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)

  /**
   * Randomise the maximum lifetime and the node size
   * @param rand
   */
  public void randomiseProperties(Random rand) {
    relativeLifetime = (float)(MathX.getTruncatedGaussian(rand, 1, AGE_VARIATION_FACTOR));
    relativeSize = (float)(MathX.getTruncatedGaussian(rand, 1, SIZE_VARIATION_FACTOR));
  }

  /**
   * Get an initial motion vector for this node, randomised around the initialDirection
   * @param initialDirection the initial direction
   * @param rand
   * @return the initial motion vector (speed and direction)
   */
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

  public boolean isDead()
  {
    return ageTicks > getMaxLifeTime();
  }

  /**
   * Update the age of the node based on what is happening (collisions) to the associated entity
   * Should be called once per tick
   * @param parentEntity the entity associated with this node
   */
  public void updateAge(Entity parentEntity)
  {
    if (parentEntity.isInWater()) {  // extinguish in water
      ageTicks = getMaxLifeTime() + 1;
      return;
    }

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

  /**
   * Change the size of the associated entity to match the node size
   * @param entity
   */
  public void changeEntitySizeToMatch(Entity entity)
  {
    // change size of entity AABB used for collisions
    // when the entity size is changed, it changes the bounding box but doesn't recentre it, so the xpos and zpos move
    //  (the entity update resetPositionToBB copies it back)
    // To fix this, we resize the AABB around the existing centre

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
