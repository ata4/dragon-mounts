package info.ata4.minecraft.dragon.server.network;

import info.ata4.minecraft.dragon.util.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

/**
 * Created by TGG on 6/07/2015.
 * The target of the dragon's breath weapon
 * Can be a world location [x,y,z], a particular entity, or a direction [x,y,z]
 */
public class BreathWeaponTarget
{
  public enum TypeOfTarget {LOCATION, ENTITY, DIRECTION};

  public static BreathWeaponTarget targetLocation(Vec3 location) {
    BreathWeaponTarget retval = new BreathWeaponTarget(TypeOfTarget.LOCATION);
    retval.coordinates = location;
    return retval;
  }

  public static BreathWeaponTarget targetEntity(Entity entity) {
    BreathWeaponTarget retval = new BreathWeaponTarget(TypeOfTarget.ENTITY);
    retval.entityID = entity.getEntityId();
    return retval;
  }

  public static BreathWeaponTarget targetEntityID(int i_entity) {
    BreathWeaponTarget retval = new BreathWeaponTarget(TypeOfTarget.ENTITY);
    retval.entityID = i_entity;
    return retval;
  }

  public static BreathWeaponTarget targetDirection(Vec3 direction) {
    BreathWeaponTarget retval = new BreathWeaponTarget(TypeOfTarget.DIRECTION);
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

  // create a BreathWeaponTarget from a ByteBuf
  public static BreathWeaponTarget fromBytes(ByteBuf buf) throws IndexOutOfBoundsException, IllegalArgumentException
  {
    int typeOfHitInt = buf.readInt();
    if (typeOfHitInt < 0 || typeOfHitInt >= TypeOfTarget.values().length) {
      throw new IllegalArgumentException("typeOfHitInt was " + typeOfHitInt);
    }
    TypeOfTarget typeOfHit = TypeOfTarget.values()[typeOfHitInt];
    BreathWeaponTarget breathWeaponTarget;
    switch (typeOfHit) {
      case DIRECTION: {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        breathWeaponTarget = BreathWeaponTarget.targetDirection(new Vec3(x, y, z));
        break;
      }
      case LOCATION: {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        breathWeaponTarget = BreathWeaponTarget.targetLocation(new Vec3(x, y, z));
        break;
      }
      case ENTITY: {
        int rawEntityID = buf.readInt();
        breathWeaponTarget = BreathWeaponTarget.targetEntityID(rawEntityID);
        break;
      }
      default: {
        throw new IllegalArgumentException("Invalid typeOfHit" + typeOfHit);
      }
    }
    return breathWeaponTarget;
  }

  /**
   * Create a target from a MovingObjectPosition
   * @param movingObjectPosition can be null
   * @return null if not possible
   */
  public static BreathWeaponTarget fromMovingObjectPosition(MovingObjectPosition movingObjectPosition, EntityPlayer entityPlayer)
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
   * Set the path navigation to head away from the given target (no effect for DIRECTION target type)
   * @param world
   * @param pathNavigate
   * @param moveSpeed
   */
  public void setNavigationPathAvoid(World world, PathNavigate pathNavigate, Vec3 currentPosition, double moveSpeed, double desiredDistance)
  {
    Vec3 target;

    switch (typeOfTarget) {
      case LOCATION: {
        target = coordinates;
        break;
      }
      case ENTITY: {
        Entity targetEntity = world.getEntityByID(entityID);
        if (targetEntity == null) return;
        target = targetEntity.getPositionVector().addVector(0, targetEntity.getEyeHeight(), 0);
        break;
      }
      case DIRECTION: {  // no need to move
        return;
      }
      default: {
        if (printedError) return;
        printedError = true;
        System.err.println("Unknown typeOfTarget:" + typeOfTarget);
        return;
      }
    }

    // choose a block at random at the desired radius from the target.  Initially try directly opposite, later on try
    //   from the entire radius around the target.

    Random random = new Random();

    final int RANDOM_TRIES = 10;
    int numberOfTries = 1;
    double deltaX = currentPosition.xCoord - target.xCoord;
    double deltaZ = currentPosition.zCoord - target.zCoord;
    double fleeAngle = Math.atan2(deltaZ, deltaX);
    do {
      double halfAngleOfSearch = Math.PI * (double)numberOfTries / (double)RANDOM_TRIES;
      double angle = fleeAngle + (random.nextFloat() * 2.0 - 1.0) * halfAngleOfSearch;
      double xDest = target.xCoord + Math.cos(angle) * desiredDistance;
      double zDest = target.zCoord + Math.sin(angle) * desiredDistance;

      int blockX = MathHelper.floor_double(xDest);
      int blockY = MathHelper.floor_double(target.yCoord);
      int blockZ = MathHelper.floor_double(zDest);

      int initBlockY = blockY;
      if (world.isAirBlock(new BlockPos(blockX, blockY, blockZ))) {
        while (blockY > 0 && world.isAirBlock(new BlockPos(blockX, blockY-1, blockZ))) {
          --blockY;
        }
      } else {
        final int MAX_BLOCK_Y = 255;
        while (blockY <= MAX_BLOCK_Y && !world.isAirBlock(new BlockPos(blockX, blockY+1, blockZ))) {
          ++blockY;
        }
        ++blockY;
      }
      int changeInY = blockY - initBlockY;
      boolean success = pathNavigate.tryMoveToXYZ(xDest, target.yCoord + changeInY, zDest, moveSpeed);
      if (success) return;
    } while (++numberOfTries <= RANDOM_TRIES);
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
   * write the BreathWeaponTarget to a ByteBuf
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
   * create a BreathWeaponTarget from a string-encoded version
   * @param targetString
   * @return the target; or null if no target
   */
  public static BreathWeaponTarget fromEncodedString(String targetString) throws IndexOutOfBoundsException, IllegalArgumentException
  {
    if (targetString.isEmpty()) return null;
    byte [] bytes = Base64.decode(targetString);
    ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
    return fromBytes(byteBuf);
  }

  /**
   * writes the BreathWeaponTarget to an encoded string
   * @return the encoded string
   */
  public String toEncodedString()
  {
    final int INITIAL_CAPACITY = 256;
    ByteBuf byteBuf = Unpooled.buffer(INITIAL_CAPACITY);
    toBytes(byteBuf);
    byte [] messageonly = Arrays.copyOf(byteBuf.array(), byteBuf.readableBytes());
    return Base64.encodeToString(messageonly, true);
  }

  /**
   * Check if these two BreathWeaponTargets are significantly different from each other
   * @param first
   * @param second
   * @return true if similar, false if not.
   */
  public static boolean approximatelyMatches(BreathWeaponTarget first, BreathWeaponTarget second)
  {
    if (first == null) {
      return (second == null);
    }
    return first.approximatelyMatches(second);
  }

  /**
   * Check if these two BreathWeaponTargets are significantly different from each other
   * @param other
   * @return true if similar, false if not.
   */
  public boolean approximatelyMatches(BreathWeaponTarget other)
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
   * Check if these two BreathWeaponTargets exactly match each other
   * @param other
   * @return
   */
  public boolean exactlyMatches(BreathWeaponTarget other)
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

//  public double getYawAngle(Vec3 dragonPosition)
//  {
//    double d0 = this.posX - this.entity.posX;
//    double d1 = this.posY - (this.entity.posY + (double)this.entity.getEyeHeight());
//    double d2 = this.posZ - this.entity.posZ;
//    double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
//    float f = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
//    float f1 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
//    this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, f1, this.deltaLookPitch);
//    this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, f, this.deltaLookYaw);
//
//  }
//


  @Override
  public String toString()
  {
    String retval = "BreathWeaponTarget(" + typeOfTarget + ") ";
    if (typeOfTarget  == TypeOfTarget.ENTITY) {
      return retval + ":" + entityID;
    }
    return retval + String.format(":[%.2f, %.2f, %.2f]",
            coordinates.xCoord, coordinates.yCoord, coordinates.zCoord);
  }

  private static boolean printedError = false;

  private BreathWeaponTarget(TypeOfTarget i_typeOfTarget)
  {
    typeOfTarget = i_typeOfTarget;
  }

  private TypeOfTarget typeOfTarget;
  private Vec3 coordinates;
  private int entityID;

}
