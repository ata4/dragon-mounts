package info.ata4.minecraft.dragon.server.network;

import info.ata4.minecraft.dragon.util.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
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
   * @return the
   */
  public static DragonOrbTarget fromEncodedString(String dragonOrbTargetString) throws IndexOutOfBoundsException, IllegalArgumentException
  {
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
  public boolean isSignificantlyDifferent(DragonOrbTarget other)
  {
    if (other.typeOfTarget != this.typeOfTarget) return true;

    switch (typeOfTarget) {
      case ENTITY: {
        return (this.entityID != other.entityID);
      }

      case LOCATION: {
        double squareDistance = this.coordinates.squareDistanceTo(other.coordinates);
        final double THRESHOLD_DISTANCE = 0.5;
        return squareDistance >= THRESHOLD_DISTANCE * THRESHOLD_DISTANCE;
      }

      case DIRECTION: {
        final double THRESHOLD_CHANGE_IN_ANGLE = 1.0; // in degrees
        double cosAngle = this.coordinates.dotProduct(other.coordinates);  // coordinates are both always normalised
        return cosAngle < Math.cos(Math.toRadians(THRESHOLD_CHANGE_IN_ANGLE));
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
    return "DragonOrbTarget(" + typeOfTarget + ")"
            + (typeOfTarget == TypeOfTarget.ENTITY ? "id=" + entityID : "coordinates=" + coordinates);
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
