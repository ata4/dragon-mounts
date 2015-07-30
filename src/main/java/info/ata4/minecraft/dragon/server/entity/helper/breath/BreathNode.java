package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Set;

/**
 * Created by TGG on 30/07/2015.
 */
public class BreathNode
{
  public BreathNode(Power i_power, Vec3 i_position, Vec3 i_direction)
  {
    setPower(i_power);
  }

  public BreathNode(Power i_power)
  {
    setPower(i_power);
  }


  public enum Power {SMALL, MEDIUM, LARGE}

  private Vec3 position;
  private Vec3 direction;
  private float ageTicks;

  private float relativeSize = 1.0F;
  private float relativeLifetime = 1.0F;

  public void setRelativeSize(double newSizeFactor)
  {
    relativeSize = (float)newSizeFactor;
  }

  public void setRelativeLifetime(double newLifetimeFactor)
  {
    relativeLifetime = (float)newLifetimeFactor;
  }

  public float getStartingSpeed()
  {
    return speedPowerFactor * INITIAL_SPEED;
  }

  public void setAgeTicks(int newAgeInTicks)
  {
    ageTicks = newAgeInTicks;
  }

  public float getMaxLifeTime()
  {
    return lifetimePowerFactor * relativeLifetime * DEFAULT_AGE_IN_TICKS;
  }

  public float getAgeTicks() {return ageTicks;}

//  public Vec3 getPosition() {
//
//  }

//  public Vec3 getDirection() {
//
//  }

  /**
   * moves the node for the indicated number of ticks
   * splits the move into a number of steps.
   * makes a record of all the blocks it touches, and the node location at each step
   */
  public void move(World world, Set<BlockPos> blocksTouched, Collection<Vec3> nodeLocations, int tickCount)
  {
    final float MAX_MOVE_DISTANCE_PER_STEP = 0.5F; // maximum number of blocks moved per step

    float distanceToMove = tickCount * getStartingSpeed();
    float numberOfStepsFrac = distanceToMove / MAX_MOVE_DISTANCE_PER_STEP;
    int numberOfSteps = (int)numberOfStepsFrac + 1;
    float stepDistance = distanceToMove / numberOfSteps;

    Vec3 startPosition = position;
    Vec3 endPosition = startPosition.addVector(direction.xCoord * stepDistance,
                                                direction.yCoord * stepDistance,
                                                direction.zCoord * stepDistance);
    final boolean STOP_ON_LIQUID = true;
    final boolean IGNORE_BOUNDING_BOX = false;
    final boolean RETURN_NULL_IF_NO_COLLIDE = true;
    MovingObjectPosition collidedBlock = world.rayTraceBlocks(startPosition, endPosition,
            STOP_ON_LIQUID, IGNORE_BOUNDING_BOX,
            !RETURN_NULL_IF_NO_COLLIDE);
    if (collidedBlock == null) {
      position = endPosition;
    } else {
      
    }

    THIS ISN'T GOING TO WORK PROPERLY; USE ENTITY MOVEMENT INSTEAD'



  }

  public boolean isDead()
  {
    return ageTicks > getMaxLifeTime();
  }


  public float getSize() {
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
