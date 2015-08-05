package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by TGG on 31/07/2015.
 */
class EntityBreathNode extends Entity
{
  public static EntityBreathNode createEntityBreathNodeServer(World world, double x, double y, double z,
                                                                    double directionX, double directionY, double directionZ,
                                                                    BreathNode.Power power)
  {
    Vec3 direction = new Vec3(directionX, directionY, directionZ).normalize();

    Random rand = new Random();
    BreathNode breathNode = new BreathNode(power);
    Vec3 actualMotion = breathNode.getRandomisedStartingMotion(direction, rand);
    // don't randomise the other properties (size, age) on the server.

    EntityBreathNode newEntity = new EntityBreathNode(world, x, y, z, actualMotion, breathNode);
    breathNode.changeEntitySizeToMatch(newEntity);
    return newEntity;
  }

  private EntityBreathNode(World world, double x, double y, double z, Vec3 motion, BreathNode i_breathNode)
  {
    super(world);
    breathNode = i_breathNode;

    final float ARBITRARY_START_SIZE = 0.2F;
    this.setSize(ARBITRARY_START_SIZE, ARBITRARY_START_SIZE);
    this.setPosition(x, y, z);
    lastTickPosX = x;
    lastTickPosY = y;
    lastTickPosZ = z;

    motionX = motion.xCoord;
    motionY = motion.yCoord;
    motionZ = motion.zCoord;
  }

  @Override
  public void onUpdate() {
    breathNode.changeEntitySizeToMatch(this); // note - will change posX, posY, posZ to keep centre constant when resizing

    handleWaterMovement();

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    moveEntity(motionX, motionY, motionZ);

    if (isCollided && onGround) {
      motionY -= 0.01F;         // ensure that we hit the ground next time too
    }
    breathNode.updateAge(this);
    if (breathNode.isDead()) {
      setDead();
    }
  }

  public float getCurrentRadius() {
    return breathNode.getCurrentSize() / 2.0F;
  }

  public float getCurrentIntensity() {return  breathNode.getCurrentIntensity();}

  private BreathNode breathNode;

  @Override
  protected void entityInit()
  {
  }

  @Override
  protected void readEntityFromNBT(NBTTagCompound tagCompund)
  {
  }

  @Override
  protected void writeEntityToNBT(NBTTagCompound tagCompound)
  {
  }
}
