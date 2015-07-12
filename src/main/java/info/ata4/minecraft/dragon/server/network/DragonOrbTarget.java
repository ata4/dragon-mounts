package info.ata4.minecraft.dragon.server.network;

import info.ata4.minecraft.dragon.util.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 6/07/2015.
 * The target of the player's dragon orb.
 * Can be a world location [x,y,z], a particular entity, or a direction [x,y,z]
 */
public class DragonOrbTarget
{
  public enum TypeOfTarget {LOCATION, ENTITY, DIRECTION};

  public static DragonOrbTarget targetLocation(Vec3 location) {
    DragonOrbTarget retval = new DragonOrbTarget(TypeOfTarget.LOCATION);
    retval.coordinates = location;
    return retval;
  }

  public static DragonOrbTarget targetEntity(Entity entity) {
    DragonOrbTarget retval = new DragonOrbTarget(TypeOfTarget.ENTITY);
    retval.entityID = entity.getEntityId();
    return retval;
  }

  public static DragonOrbTarget targetEntityID(int i_entity) {
    DragonOrbTarget retval = new DragonOrbTarget(TypeOfTarget.ENTITY);
    retval.entityID = i_entity;
    return retval;
  }

  public static DragonOrbTarget targetDirection(Vec3 direction) {
    DragonOrbTarget retval = new DragonOrbTarget(TypeOfTarget.DIRECTION);
    retval.coordinates = direction.normalize();
    return retval;
  }

  public TypeOfTarget getTypeOfTarget() {return  typeOfTarget;}

  /**
   *  get the entity being targeted
   * @param world
   * @return null if not found or not valid
   */
  public Entity getTargetEntity(World world) {
    return world.getEntityByID(entityID);
  }

  public Vec3 getTargetedLocation()
  {
    return new Vec3(coordinates.xCoord, coordinates.yCoord, coordinates.zCoord);
  }

  public Vec3 getTargetedDirection()
  {
    return new Vec3(coordinates.xCoord, coordinates.yCoord, coordinates.zCoord);
  }

  // create a DragonOrbTarget from a ByteBuf
  public static DragonOrbTarget fromBytes(ByteBuf buf) throws IndexOutOfBoundsException, IllegalArgumentException
  {
    int typeOfHitInt = buf.readInt();
    if (typeOfHitInt < 0 || typeOfHitInt >= TypeOfTarget.values().length) {
      throw new IllegalArgumentException("typeOfHitInt was " + typeOfHitInt);
    }
    TypeOfTarget typeOfHit = TypeOfTarget.values()[typeOfHitInt];
    DragonOrbTarget dragonOrbTarget;
    switch (typeOfHit) {
      case DIRECTION: {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        dragonOrbTarget = DragonOrbTarget.targetDirection(new Vec3(x, y, z));
        break;
      }
      case LOCATION: {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        dragonOrbTarget = DragonOrbTarget.targetLocation(new Vec3(x, y, z));
        break;
      }
      case ENTITY: {
        int rawEntityID = buf.readInt();
        dragonOrbTarget = DragonOrbTarget.targetEntityID(rawEntityID);
        break;
      }
      default: {
        throw new IllegalArgumentException("Invalid typeOfHit" + typeOfHit);
      }
    }
    return dragonOrbTarget;
  }

  /**
   * Create a target from a MovingObjectPosition
   * @param movingObjectPosition can be null
   * @return null if not possible
   */
  public static DragonOrbTarget fromMovingObjectPosition(MovingObjectPosition movingObjectPosition, EntityPlayer entityPlayer)
  {
    if (movingObjectPosition == null) {
      return targetDirection(entityPlayer.getLook(1.0F));
    }
    switch (movingObjectPosition.typeOfHit) {
      case BLOCK: {
        return targetLocation(movingObjectPosition.hitVec);
      }
      case ENTITY: {
        return targetEntity(movingObjectPosition.entityHit);
      }
      case MISS: {
        return targetDirection(entityPlayer.getLook(1.0F));
      }
      default: {
        if (printedError) return null;
        printedError = true;
        System.err.println("Unknown typeOfHit:" + movingObjectPosition.typeOfHit);
        return null;
      }
    }
  }

  /**
   * Sets where the entity is looking, based on the target
   * @param world
   * @param entityLookHelper
   * @param yawSpeed speed of head yaw change
   * @param pitchSpeed speed of head pitch change
   */
  public void setEntityLook(World world, EntityLookHelper entityLookHelper, float yawSpeed, float pitchSpeed)
  {
    switch (typeOfTarget) {
      case LOCATION: {
        entityLookHelper.setLookPosition(coordinates.xCoord, coordinates.yCoord, coordinates.zCoord,
                                         yawSpeed, pitchSpeed);
        break;
      }
      case ENTITY: {
        Entity targetEntity = world.getEntityByID(entityID);
        if (targetEntity != null) {
          entityLookHelper.setLookPositionWithEntity(targetEntity, yawSpeed, pitchSpeed);
        }
        break;
      }
      case DIRECTION: {  // simulate a look direction by choosing a very-far-away point
        double entityX = entityLookHelper.func_180423_e();
        double entityY = entityLookHelper.func_180422_f();
        double entityZ = entityLookHelper.func_180421_g();
        final double FAR_DISTANCE = 1000;
        entityLookHelper.setLookPosition(entityX + FAR_DISTANCE * coordinates.xCoord,
                                         entityY + FAR_DISTANCE * coordinates.yCoord,
                                         entityZ + FAR_DISTANCE * coordinates.zCoord,
                                         yawSpeed, pitchSpeed);
        break;
      }
      default: {
        if (printedError) return;
        printedError = true;
        System.err.println("Unknown typeOfTarget:" + typeOfTarget);
        break;
      }
    }
  }

  /**
   * Set the path navigation to head towards the given target (no effect for DIRECTION target type)
   * @param world
   * @param pathNavigate
   * @param moveSpeed
   */
  public void setNavigationPath(World world, PathNavigate pathNavigate, double moveSpeed)
  {
    switch (typeOfTarget) {
      case LOCATION: {
        pathNavigate.tryMoveToXYZ(coordinates.xCoord, coordinates.yCoord, coordinates.zCoord, moveSpeed);
        break;
      }
      case ENTITY: {
        Entity targetEntity = world.getEntityByID(entityID);
        if (targetEntity != null) {
          pathNavigate.tryMoveToEntityLiving(targetEntity, moveSpeed);
        }
        break;
      }
      case DIRECTION: {  // no need to move
        break;
      }
      default: {
        if (printedError) return;
        printedError = true;
        System.err.println("Unknown typeOfTarget:" + typeOfTarget);
        break;
      }
    }
  }

  /**
   * calculate the distance from the given point to the target
   * @param world
   * @return distance squared to the target, or -ve number if not relevant (eg target type DIRECTION)
   */
  public double distanceSQtoTarget(World world, Vec3 startPoint)
  {
    switch (typeOfTarget) {
      case LOCATION: {
        return startPoint.squareDistanceTo(coordinates);
      }
      case ENTITY: {
        Entity targetEntity = world.getEntityByID(entityID);
        if (targetEntity != null) {
          return startPoint.squareDistanceTo(targetEntity.getPositionVector());
        } else {
          return -1;
        }
      }
      case DIRECTION: {  // no need to move
        return -1;
      }
      default: {
        if (printedError) return -1;
        printedError = true;
        System.err.println("Unknown typeOfTarget:" + typeOfTarget);
        return -1;
      }
    }
  }

  /** get the point being targeted in [x,y,z]
   * @param world
   * @param origin the origin of the breath weapon (dragon's throat)
   * @return an [x,y,z] to fire the beam at; or null if none
   */
  public Vec3 getTargetedPoint(World world, Vec3 origin) {
    Vec3 destination = null;
    switch (typeOfTarget) {
      case LOCATION: {
        destination = getTargetedLocation();
        break;
      }
      case DIRECTION: {
        destination = origin.add(getTargetedDirection());
        break;
      }
      case ENTITY: {
        Entity entity = getTargetEntity(world);
        if (entity == null) {
          destination = null;
        } else {
          destination = entity.getPositionVector().addVector(0, entity.getEyeHeight() / 2.0, 0);
        }
        break;
      }
      default: {
        System.err.println("Unexpected target type:" + typeOfTarget);
        destination = null;
        break;
      }
    }
    return destination;
  }

  /**
   * write the DragonOrbTarget to a ByteBuf
   * @param buf
   */
  public void toBytes(ByteBuf buf) {
    buf.writeInt(typeOfTarget.ordinal());
    switch (typeOfTarget) {
      case LOCATION:
      case DIRECTION: {
        buf.writeDouble(coordinates.xCoord);
        buf.writeDouble(coordinates.yCoord);
        buf.writeDouble(coordinates.zCoord);
        break;
      }
      case ENTITY: {
        buf.writeInt(entityID);
        break;
      }
      default: {
        if (printedError) break;
        printedError = true;
        System.err.println("Unknown type of hit:" + typeOfTarget);
        break;
      }
    }
  }

  /**
   * create a DragonOrbTarget from a string-encoded version
   * @param dragonOrbTargetString
   * @return the target; or null if no target
   */
  public static DragonOrbTarget fromEncodedString(String dragonOrbTargetString) throws IndexOutOfBoundsException, IllegalArgumentException
  {
    if (dragonOrbTargetString.isEmpty()) return null;
    byte [] bytes = Base64.decode(dragonOrbTargetString);
    ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
    return fromBytes(byteBuf);
  }

  /**
   * writes the DragonOrbTarget to an encoded string
   * @return the encoded string
   */
  public String toEncodedString()
  {
    final int INITIAL_CAPACITY = 256;
    ByteBuf byteBuf = Unpooled.buffer(INITIAL_CAPACITY);
    toBytes(byteBuf);
    return Base64.encodeToString(byteBuf.array(), true);
  }



  /**
   * Check if these two DragonOrbTargets are significantly different from each other
   * @param other
   * @return
   */
  public boolean approximatelyMatches(DragonOrbTarget other)
  {
    if (other == null) return false;
    if (other.typeOfTarget != this.typeOfTarget) return false;

    switch (typeOfTarget) {
      case ENTITY: {
        return (this.entityID == other.entityID);
      }

      case LOCATION: {
        double squareDistance = this.coordinates.squareDistanceTo(other.coordinates);
        final double THRESHOLD_DISTANCE = 0.5;
        return squareDistance < THRESHOLD_DISTANCE * THRESHOLD_DISTANCE;
      }

      case DIRECTION: {
        final double THRESHOLD_CHANGE_IN_ANGLE = 1.0; // in degrees
        double cosAngle = this.coordinates.dotProduct(other.coordinates);  // coordinates are both always normalised
        return cosAngle > Math.cos(Math.toRadians(THRESHOLD_CHANGE_IN_ANGLE));
      }
      default: {
        if (printedError) return false;
        printedError = true;
        System.err.println("invalid typeOfTarget:" + typeOfTarget);
        return false;
      }
    }
  }

  /**
   * Check if these two DragonOrbTargets exactly match each other
   * @param other
   * @return
   */
  public boolean exactlyMatches(DragonOrbTarget other)
  {
    if (other.typeOfTarget != this.typeOfTarget) return false;
    switch (typeOfTarget) {
      case ENTITY: {
        return (this.entityID == other.entityID);
      }

      case DIRECTION:
      case LOCATION: {
        return (this.coordinates.xCoord == other.coordinates.xCoord
                && this.coordinates.yCoord == other.coordinates.yCoord
                && this.coordinates.zCoord == other.coordinates.zCoord);
      }
      default: {
        if (printedError) return false;
        printedError = true;
        System.err.println("invalid typeOfTarget:" + typeOfTarget);
        return false;
      }
    }
  }

  @Override
  public String toString()
  {
    String retval = "DragonOrbTarget(" + typeOfTarget + ") ";
    if (typeOfTarget  == TypeOfTarget.ENTITY) {
      return retval + ":" + entityID;
    }
    return retval + String.format(":[%.2f, %.2f, %.2f]",
            coordinates.xCoord, coordinates.yCoord, coordinates.zCoord);
  }

  private static boolean printedError = false;

  private DragonOrbTarget(TypeOfTarget i_typeOfTarget)
  {
    typeOfTarget = i_typeOfTarget;
  }

  private TypeOfTarget typeOfTarget;
  private Vec3 coordinates;
  private int entityID;

}
