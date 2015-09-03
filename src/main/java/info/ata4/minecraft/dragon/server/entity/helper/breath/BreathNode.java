package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.Random;

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

  private float relativeSizeOfThisNode = 1.0F;
  private float relativeLifetimeOfThisNode = 1.0F;

  private static final double SPEED_VARIATION_ABS = 0.1;  // plus or minus this amount (3 std deviations)  //todo reset to 0.1
  private static final double AGE_VARIATION_FACTOR = 0.25;//0.25;   // plus or minus this amount (3 std deviations) //todo reset to 0.25
  private static final double SIZE_VARIATION_FACTOR = 0.25; // 0.25;   // plus or minus this amount (3 std deviations)todo reset to 0.25

  /**
   * Randomise the maximum lifetime and the node size
   * @param rand
   */
  public void randomiseProperties(Random rand) {
    relativeLifetimeOfThisNode = (float)(MathX.getTruncatedGaussian(rand, 1, AGE_VARIATION_FACTOR));
    relativeSizeOfThisNode = (float)(MathX.getTruncatedGaussian(rand, 1, SIZE_VARIATION_FACTOR));
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
    return lifetimePowerFactor * relativeLifetimeOfThisNode * DEFAULT_AGE_IN_TICKS;
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

//    if (ageTicks > 0) return;     //todo remove

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

//  /**
//   * Change the size of the associated entity to match the node size
//   * @param entity
//   */
//  @Deprecated
//  public void changeEntitySizeToMatch(Entity entity)
//  {
//    // change size of entity AABB used for collisions
//    // when the entity size is changed, it changes the bounding box but doesn't recentre it, so the xpos and zpos move
//    //  (the entity update resetPositionToBB copies it back)
//    // To fix this, we resize the AABB around the existing centre
//    // The AABB is centred around the entity xpos and zpos, but the minimum y is set equal to the entity ypos
//
//    float currentNodeAABBSize = getCurrentAABBcollisionSize();
//    float width = currentNodeAABBSize;
//    float height = currentNodeAABBSize;
//
//    if (width != entity.width || height != entity.height) {
//      AxisAlignedBB oldAABB = entity.getEntityBoundingBox();
//      double oldMidptX = (oldAABB.minX + oldAABB.maxX)/2.0;
//      double oldMidptY = (oldAABB.minY + oldAABB.maxY)/2.0;
//      double oldMidptZ = (oldAABB.minZ + oldAABB.maxZ)/2.0;
//
//      entity.width = width;
//      entity.height = height;
//
//      AxisAlignedBB newAABB = new AxisAlignedBB(oldMidptX - width / 2.0, oldMidptY - height / 2.0, oldMidptZ - width / 2.0,
//                                                oldMidptX + width / 2.0, oldMidptY + height / 2.0, oldMidptZ + width / 2.0);
//      entity.setEntityBoundingBox(newAABB);
//    }
//  }

  private final float RATIO_OF_RENDER_DIAMETER_TO_EFFECT_DIAMETER = 1.0F;
  private final float RATIO_OF_COLLISION_DIAMETER_TO_EFFECT_DIAMETER = 0.5F;  // change to 0.5F

  /** getChangeInValue render size (diameter) of the breathnode in blocks
   * @return the rendering size (diameter) of the breathnode in blocks
   */
  public float getCurrentRenderDiameter() {
    return getCurrentDiameterOfEffect() * RATIO_OF_RENDER_DIAMETER_TO_EFFECT_DIAMETER;
  }

  /** getChangeInValue the width and height of the breathnode collision AABB, in blocks
   * @return the width and height of the breathnode collision AABB, in blocks
   */
  public float getCurrentAABBcollisionSize() {
    return getCurrentDiameterOfEffect() * RATIO_OF_COLLISION_DIAMETER_TO_EFFECT_DIAMETER;
  }

  /** getChangeInValue the size (diameter) of the area of effect of the breath node, in blocks
   * @return the size (diameter) of the area of effect of the breathnode in blocks
   */
  public float getCurrentDiameterOfEffect() {
    float lifetimeFraction = getLifetimeFraction();

    float fractionOfFullSize = 1.0F;
    if (lifetimeFraction < YOUNG_AGE) {
      fractionOfFullSize = MathHelper.sin(lifetimeFraction / YOUNG_AGE * (float) Math.PI / 2.0F);
    }

    final float NODE_MAX_SIZE = NODE_DIAMETER_IN_BLOCKS * sizePowerFactor * relativeSizeOfThisNode;
    final float INITIAL_SIZE = 0.2F * NODE_MAX_SIZE;
    return INITIAL_SIZE + (NODE_MAX_SIZE - INITIAL_SIZE) * MathHelper.clamp_float(fractionOfFullSize, 0.0F, 1.0F);
  }


  /** returns the current intensity of the node (eg for flame = how hot it is)
   * @return current relative intensity - 0.0 = none, 1.0 = full
   */
  public float getCurrentIntensity()
  {
    float lifetimeFraction = getLifetimeFraction();

    float fractionOfFullPower = 1.0F;
    if (lifetimeFraction < YOUNG_AGE) {
      fractionOfFullPower = MathHelper.sin(lifetimeFraction / YOUNG_AGE * (float) Math.PI / 2.0F);
    } else if (lifetimeFraction >= 1.0F) {
      fractionOfFullPower = 0.0F;
    } else if (lifetimeFraction > OLD_AGE) {
      fractionOfFullPower = MathHelper.sin((1.0F - lifetimeFraction) / (1.0F - OLD_AGE) * (float) Math.PI / 2.0F);
    }

    return fractionOfFullPower * intensityPowerFactor;
  }


  public float getLifetimeFraction() {
    float lifetimeFraction = (float)ageTicks / getMaxLifeTime();
    lifetimeFraction = MathHelper.clamp_float(lifetimeFraction, 0.0F, 1.0F);
    return lifetimeFraction;
  }

  private static final float INITIAL_SPEED = 1.2F; // blocks per tick at full speed
  private static final float NODE_DIAMETER_IN_BLOCKS = 2.0F;
  private static final int DEFAULT_AGE_IN_TICKS = 40;

  private final float YOUNG_AGE = 0.25F;
  private final float OLD_AGE = 0.75F;

  private Power power;
  private float speedPowerFactor = 1.0F;
  private float lifetimePowerFactor = 1.0F;
  private float sizePowerFactor = 1.0F;
  private float intensityPowerFactor = 1.0F;

  private void setPower(Power newPower) {
    power = newPower;
    switch (newPower) {
      case SMALL: {
        speedPowerFactor = 0.25F;
        lifetimePowerFactor = 0.25F;
        sizePowerFactor = 0.25F;
        intensityPowerFactor = 0.10F;
        break;
      }
      case MEDIUM: {
        speedPowerFactor = 0.5F;
        lifetimePowerFactor = 0.5F;
        sizePowerFactor = 0.5F;
        intensityPowerFactor = 0.25F;
        break;
      }
      case LARGE: {
        speedPowerFactor = 1.0F;
        lifetimePowerFactor = 1.0F;
        sizePowerFactor = 1.0F;
        intensityPowerFactor = 1.0F;
        break;
      }

      default: {
        System.err.println("Invalid power in setPower:" + newPower);
      }
    }
  }


}
