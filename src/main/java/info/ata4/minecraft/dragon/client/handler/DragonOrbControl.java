/*
 ** 2013 October 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.handler;

        import info.ata4.minecraft.dragon.DragonMounts;
        import info.ata4.minecraft.dragon.server.CommonProxy;
        import info.ata4.minecraft.dragon.server.ItemDragonOrb;
        import info.ata4.minecraft.dragon.server.network.DragonControlMessage;
        import info.ata4.minecraft.dragon.server.util.ItemUtils;
        import net.minecraft.client.Minecraft;
        import net.minecraft.client.entity.EntityPlayerSP;
        import net.minecraft.client.settings.KeyBinding;
        import net.minecraft.entity.Entity;
        import net.minecraft.entity.EntityLivingBase;
        import net.minecraft.entity.item.EntityItemFrame;
        import net.minecraft.entity.player.EntityPlayer;
        import net.minecraft.item.ItemStack;
        import net.minecraft.util.*;
        import net.minecraft.world.World;
        import net.minecraftforge.fml.client.registry.ClientRegistry;
        import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
        import net.minecraftforge.fml.common.gameevent.TickEvent;
        import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
        import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
        import org.lwjgl.input.Keyboard;

        import java.util.BitSet;
        import java.util.List;

/**
 * If the player is holding the dragon orb, records whether the player is holding down the
 *   trigger and what the current target is (where the player is pointing the cursor)
 */
public class DragonOrbControl {

  private SimpleNetworkWrapper network;

  static public DragonOrbControl createSingleton(SimpleNetworkWrapper i_network) {
    instance = new DragonOrbControl(i_network);
    return instance;
  }

  static public DragonOrbControl getInstance() {
    return instance;
  }

  /**
   * Every tick, check if the player is holding the Dragon Orb, and if so, whether the player is targeting someone with it
   * @param evt
   */
  @SubscribeEvent
  public void onTick(ClientTickEvent evt) {
    if (evt.phase != ClientTickEvent.Phase.START) return;
    EntityPlayerSP entityPlayerSP = Minecraft.getMinecraft().thePlayer;
    if (entityPlayerSP == null) return;

    MovingObjectPosition oldMOP = movingObjectPosition;
    boolean oldTriggerHeld = triggerHeld;

    if (!ItemUtils.hasEquipped(entityPlayerSP, DragonMounts.proxy.itemDragonOrb)) {
      triggerHeld = false;
    } else {
      triggerHeld = entityPlayerSP.isUsingItem();
      if (triggerHeld) {
        final float MAX_ORB_RANGE = 20.0F;
        MovingObjectPosition mop = getMouseOver(entityPlayerSP.getEntityWorld(), entityPlayerSP, MAX_ORB_RANGE);
        if (mop == null) {
          mop = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS,
                                         entityPlayerSP.getLook(1.0F),
                                         EnumFacing.NORTH, new BlockPos(0, 0, 0));    // arbitrary
        }
        movingObjectPosition = mop;
      }
    }

    if (!triggerHeld) {
      if (oldTriggerHeld) {

      }
    }
    if (ticksSinceLastMessage >
            triggerHeld != oldTriggerHeld
        || (triggerHeld && )    )

  }

  private final int MAX_TIME_NO_MESSAGE = 20;  // send a message at least this many ticks or less
  private int ticksSinceLastMessage = 0;
  /**
   * Get the block or entity being targeted by the dragon orb
   * @return MovingObjectPosition with types:
   *   BLOCK - the block at blockPos
   *   ENTITY - the entity at hitVec
   *   MISS - no target, hitVec contains the player's look direction.
   *   null = no target.
   */
  public MovingObjectPosition getTarget()
  {
    if (triggerHeld) {
      return movingObjectPosition;
    } else {
      return null;
    }
  }

  /**
   * Find what the player is looking at (block or entity), up to a maximum range
   * based on code from EntityRenderer.getMouseOver
   * @return the block or entity that the player is looking at / targeting with their cursor.  null if no collision
   */
  private MovingObjectPosition getMouseOver(World world, EntityPlayerSP entityPlayerSP, float maxDistance) {
    final float PARTIAL_TICK = 1.0F;
    Vec3 positionEyes = entityPlayerSP.getPositionEyes(PARTIAL_TICK);
    Vec3 lookDirection = entityPlayerSP.getLook(PARTIAL_TICK);
    Vec3 endOfLook = positionEyes.addVector(lookDirection.xCoord * maxDistance,
                                            lookDirection.yCoord * maxDistance,
                                            lookDirection.zCoord * maxDistance);
    final boolean STOP_ON_LIQUID = true;
    final boolean IGNORE_BOUNDING_BOX = false;
    final boolean RETURN_NULL_IF_NO_COLLIDE = true;
    MovingObjectPosition targetedBlock = world.rayTraceBlocks(positionEyes, endOfLook,
                                                              STOP_ON_LIQUID, IGNORE_BOUNDING_BOX,
                                                              !RETURN_NULL_IF_NO_COLLIDE);

    double collisionDistanceSQ = maxDistance * maxDistance;
    if (targetedBlock != null) {
      collisionDistanceSQ = targetedBlock.hitVec.squareDistanceTo(positionEyes);
      endOfLook = targetedBlock.hitVec;
    }

    final float EXPAND_SEARCH_BOX_BY = 1.0F;
    AxisAlignedBB searchBox = entityPlayerSP.getEntityBoundingBox();
    Vec3 endOfLookDelta = endOfLook.subtract(positionEyes);
    searchBox = searchBox.addCoord(endOfLookDelta.xCoord, endOfLookDelta.yCoord, endOfLookDelta.zCoord);
    searchBox = searchBox.expand(EXPAND_SEARCH_BOX_BY, EXPAND_SEARCH_BOX_BY, EXPAND_SEARCH_BOX_BY);
    List<Entity> nearbyEntities = (List<Entity>) world.getEntitiesWithinAABBExcludingEntity(
            entityPlayerSP, searchBox);
    Entity closestEntityHit = null;
    double closestEntityDistanceSQ = Double.MAX_VALUE;
    for (Entity entity : nearbyEntities) {
      if (!entity.canBeCollidedWith() || entity == entityPlayerSP.ridingEntity) {
        continue;
      }

      float collisionBorderSize = entity.getCollisionBorderSize();
      AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
                                          .expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
      MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(positionEyes, endOfLook);

      if (axisalignedbb.isVecInside(endOfLook)) {
        double distanceSQ = (movingobjectposition == null) ? positionEyes.squareDistanceTo(endOfLook)
                                                           : positionEyes.squareDistanceTo(movingobjectposition.hitVec);
        if (distanceSQ <= closestEntityDistanceSQ) {
          closestEntityDistanceSQ = distanceSQ;
          closestEntityHit = entity;
        }
      } else if (movingobjectposition != null) {
        double distanceSQ = positionEyes.squareDistanceTo(movingobjectposition.hitVec);
        if (distanceSQ <= closestEntityDistanceSQ) {
          closestEntityDistanceSQ = distanceSQ;
          closestEntityHit = entity;
        }
      }
    }

    if (closestEntityDistanceSQ <= collisionDistanceSQ) {
      assert (closestEntityHit != null);
      return new MovingObjectPosition(closestEntityHit, closestEntityHit.getPositionVector());
    }
    return targetedBlock;
  }


  private boolean triggerHeld = false;
  private MovingObjectPosition movingObjectPosition;

  private static DragonOrbControl instance = null;

  private DragonOrbControl(SimpleNetworkWrapper i_network) {
    network = i_network;
  }
}
