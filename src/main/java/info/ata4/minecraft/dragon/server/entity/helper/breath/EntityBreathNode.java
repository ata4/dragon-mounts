package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.EntityMoveAndResizeHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.HashMap;
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
    entityMoveAndResizeHelper = new EntityMoveAndResizeHelper(this);
  }

  @Override
  public void onUpdate() {

    handleWaterMovement();

    float newAABBDiameter = breathNode.getCurrentAABBcollisionSize();

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    collisions = entityMoveAndResizeHelper.moveAndResizeEntity(motionX, motionY, motionZ, newAABBDiameter, newAABBDiameter);

    if (isCollided && onGround) {
      motionY -= 0.01F;         // ensure that we hit the ground next time too
    }
    breathNode.updateAge(this);
    if (breathNode.isDead()) {
      setDead();
    }
  }

  public float getCurrentRadius() {
    return breathNode.getCurrentDiameterOfEffect() / 2.0F;
  }

  public float getCurrentIntensity() {return  breathNode.getCurrentIntensity();}

  /**
   * Get a collection of the collisions that occurred during the last tick update
   *@return returns a collection showing which parts of the entity collided with an object- eg
   *        (WEST, [3,2,6]-->[3.5, 2, 6] means the west face of the entity collided; the entity tried to move to
   *          x = 3, but got pushed back to x=3.5
   */
  public HashMap<EnumFacing, AxisAlignedBB> getRecentCollisions()
  {
    if (collisions == null) {
      collisions = new HashMap<EnumFacing, AxisAlignedBB>();
    }
    return collisions;
  }

  private BreathNode breathNode;
  private EntityMoveAndResizeHelper entityMoveAndResizeHelper;

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

  private HashMap<EnumFacing, AxisAlignedBB> collisions;

}
