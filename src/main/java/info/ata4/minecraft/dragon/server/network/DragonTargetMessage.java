/*
 ** 2014 March 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.BitSet;

/**
 * Message to tell dragon what to target with their ranged breath weapon.
 *   Sent from client to server only (server to client is by datawatcher)
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonTargetMessage implements IMessage {

  /**
   * Creates a message saying 'nothing is targeted'
   * @return the message for sending
   */
    public static DragonTargetMessage createUntargetMessage()
    {
      DragonTargetMessage retval = new DragonTargetMessage();
      retval.targeting = false;
      retval.packetIsValid = true;
      return retval;
    }


  /** Creates a message specifying the current target
   * @param i_target the target
   * @return the message for sending
   */
    public static DragonTargetMessage createTargetMessage(MovingObjectPosition i_target)
    {
      DragonTargetMessage retval = new DragonTargetMessage();
      retval.targeting = true;
      retval.target = i_target;
      retval.packetIsValid = true;
      return retval;
    }

    // create a new message (used by SimpleNetworkWrapper)
    public DragonTargetMessage()
    {
      packetIsValid = false;
    }

  public boolean isPacketIsValid() {
    return packetIsValid;
  }


  public boolean isTargeting() {
    return targeting;
  }

  public MovingObjectPosition getTarget(World world) {
    if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
      target.entityHit = world.getEntityByID(rawEntityID);
    }
    return target;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    packetIsValid = false;
    try {
      targeting = buf.readBoolean();
      if (targeting) {
        int typeOfHitInt = buf.readInt();
        if (typeOfHitInt < 0 || typeOfHitInt >= MovingObjectPosition.MovingObjectType.values().length) {
          return;
        }
        MovingObjectPosition.MovingObjectType typeOfHit = MovingObjectPosition.MovingObjectType.values()[typeOfHitInt];
        switch (typeOfHit) {
          case MISS: {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            target = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS,
                                              new Vec3(x, y, z),
                                              EnumFacing.NORTH, new BlockPos(0, 0, 0));    // arbitrary
            break;
          }
          case BLOCK: {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            target = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.BLOCK,
                                              new Vec3(x + 0.5, y + 0.5, z + 0.5),          // arbitrary
                                              EnumFacing.NORTH, new BlockPos(x, y, z));    // arbitrary facing
            break;
          }
          case ENTITY: {
            rawEntityID = buf.readInt();
            Entity dummy = null;
            target = new MovingObjectPosition(dummy, new Vec3(0, 0, 0));    // entity will be filled in later when retrieving target
            break;
          }
          default: {
            if (printedError) {
              break;
            }
            printedError = true;
            System.err.println("Unknown type of hit:" + target.typeOfHit);
            break;
          }
        }
      }
    } catch (IndexOutOfBoundsException ioe) {
      System.err.println("Exception while reading DragonTargetMessage: " + ioe);
      return;
    }
    packetIsValid = true;
  }


  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeBoolean(targeting);
    if (!targeting) return;
    buf.writeInt(target.typeOfHit.ordinal());
    switch (target.typeOfHit) {
      case MISS: {
        buf.writeDouble(target.hitVec.xCoord);
        buf.writeDouble(target.hitVec.yCoord);
        buf.writeDouble(target.hitVec.zCoord);
        break;
      }
      case BLOCK: {
        BlockPos targetedBlock = target.getBlockPos();
        buf.writeInt(targetedBlock.getX());
        buf.writeInt(targetedBlock.getY());
        buf.writeInt(targetedBlock.getZ());
        break;
      }
      case ENTITY: {
        buf.writeInt(target.entityHit.getEntityId());
        break;
      }
      default: {
        if (printedError) break;
        printedError = true;
        System.err.println("Unknown type of hit:" + target.typeOfHit);
        break;
      }
    }
  }


  private MovingObjectPosition target;
  private boolean targeting;
  private static boolean printedError = false;
  private boolean packetIsValid = false;
  private int rawEntityID;
}
