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
        import net.minecraft.util.AxisAlignedBB;
        import net.minecraft.util.MathHelper;
        import net.minecraft.util.MovingObjectPosition;
        import net.minecraft.util.Vec3;
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

  public DragonOrbControl getInstance() {
    if (instance == null) {
      instance = new DragonOrbControl();
    }
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
    if (!ItemUtils.hasEquipped(entityPlayerSP, DragonMounts.proxy.itemDragonOrb));
    triggerHeld = entityPlayerSP.isUsingItem();
    if (triggerHeld) {

    }
  }


  /**
   * Find what the player is looking at (block or entity), up to a maximum range
   * @param worldIn
   * @param playerIn
   * @param useLiquids
   * @return
   */
  public MovingObjectPosition getMouseOver(World world, EntityPlayerSP entityPlayerSP, float maxDistance)
  {
    final float PARTIAL_TICK = 1.0F;
    Vec3 positionEyes = entityPlayerSP.getPositionEyes(PARTIAL_TICK);
    Vec3 lookDirection = entityPlayerSP.getLook(PARTIAL_TICK);
    Vec3 endOfLook = positionEyes.addVector(lookDirection.xCoord * maxDistance,
                                            lookDirection.yCoord * maxDistance,
                                            lookDirection.zCoord * maxDistance);
    final boolean STOP_ON_LIQUID = false;
    final boolean IGNORE_BOUNDING_BOX = false;
    final boolean RETURN_NULL_IF_NO_COLLIDE = true;
    MovingObjectPosition targetedBlock = world.rayTraceBlocks(positionEyes, endOfLook,
            STOP_ON_LIQUID, IGNORE_BOUNDING_BOX, !RETURN_NULL_IF_NO_COLLIDE);

    double collisionDistanceSQ = maxDistance * maxDistance;
    if (targetedBlock != null) {
      collisionDistanceSQ = targetedBlock.hitVec.squareDistanceTo(positionEyes);
      endOfLook = targetedBlock.hitVec;
    }

//    Entity entity = this.mc.getRenderViewEntity();
//
//    if (entity != null)
//    {
//      if (this.mc.theWorld != null)
//      {
//        this.mc.mcProfiler.startSection("pick");
//        this.mc.pointedEntity = null;
//        double d0 = (double)this.mc.playerController.getBlockReachDistance();
//        this.mc.objectMouseOver = entity.rayTrace(d0, p_78473_1_);
//
//
//
//
//        double d1 = d0;
//        Vec3 vec3 = entity.getPositionEyes(p_78473_1_);
//
//        if (this.mc.playerController.extendedReach())
//        {
//          d0 = 6.0D;
//          d1 = 6.0D;
//        }
//        else
//        {
//          if (d0 > 3.0D)
//          {
//            d1 = 3.0D;
//          }
//
//          d0 = d1;
//        }
//
//        if (this.mc.objectMouseOver != null)
//        {
//          d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
//        }
//
//        Vec3 vec31 = entity.getLook(p_78473_1_);
//        Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
//        this.pointedEntity = null;
//        Vec3 vec33 = null;
//        float f1 = 1.0F;

        final float EXPAND_SEARCH_BOX_BY = 1.0F;
        AxisAlignedBB searchBox = entityPlayerSP.getEntityBoundingBox();
        searchBox = searchBox.addCoord(endOfLook.xCoord, endOfLook.yCoord, endOfLook.zCoord);
        searchBox = searchBox.expand(EXPAND_SEARCH_BOX_BY, EXPAND_SEARCH_BOX_BY, EXPAND_SEARCH_BOX_BY);
        List<Entity> nearbyEntities = (List<Entity>)world.getEntitiesWithinAABBExcludingEntity(
                                                          entityPlayerSP, searchBox);
        for (Entity entity : nearbyEntities) {
          if (!entity.canBeCollidedWith()) {
            continue;
          }

          float collisionBorderSize = entity.getCollisionBorderSize();
          AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
                                              .expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
          MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(positionEyes, endOfLook);

            if (axisalignedbb.isVecInside(vec3))
            {
              if (0.0D < d2 || d2 == 0.0D)
              {
                this.pointedEntity = entity1;
                vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                d2 = 0.0D;
              }
            }
            else if (movingobjectposition != null)
            {
              double d3 = vec3.distanceTo(movingobjectposition.hitVec);

              if (d3 < d2 || d2 == 0.0D)
              {
                if (entity1 == entity.ridingEntity && !entity.canRiderInteract())
                {
                  if (d2 == 0.0D)
                  {
                    this.pointedEntity = entity1;
                    vec33 = movingobjectposition.hitVec;
                  }
                }
                else
                {
                  this.pointedEntity = entity1;
                  vec33 = movingobjectposition.hitVec;
                  d2 = d3;
                }
              }
            }
          }




        double d2 = d1;

        for (int i = 0; i < list.size(); ++i)
        {
          Entity entity1 = (Entity)list.get(i);

        }

        if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null))
        {
          this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);

          if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame)
          {
            this.mc.pointedEntity = this.pointedEntity;
          }
        }

        this.mc.mcProfiler.endSection();
      }
    }
  }


  private boolean triggerHeld = false;
  private MovingObjectPosition movingObjectPosition;

  private DragonOrbControl instance = null;

  private DragonOrbControl() {
  }
}
