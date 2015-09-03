package info.ata4.minecraft.dragon.client.handler;

import com.google.common.collect.Lists;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.DragonMountsConfig;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathWeapon;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.network.DragonOrbTargets;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TGG on 28/08/2015.
 */
public class TargetHighlighter
{
  /**
   * If the player is holding the dragon orb but not currently holding the attack button down, override the normal
   *   highlighting with a custom highlighting. Otherwise, revert to the default.
   * @param event
   */
  @SubscribeEvent
  public void blockHighlightDecider(DrawBlockHighlightEvent event)
  {
    BreathWeaponTarget targetBeingLookedAt = DragonOrbControl.getInstance().getTargetBeingLookedAt();
    if (targetBeingLookedAt == null) {
      return;
    }

    event.setCanceled(true);

    if (!DragonMounts.instance.getConfig().isOrbHighlightTarget()) {
      return;
    }

    BreathWeaponTarget targetToHighlight = targetBeingLookedAt;
    BreathWeaponTarget targetLockedOn = DragonOrbControl.getInstance().getTargetLockedOn();
    if (targetLockedOn != null) {
      targetToHighlight = targetLockedOn;
    }

//    World world = event.player.worldObj;
//    List loadedEntities = world.getLoadedEntityList();
//    for (Object entity : loadedEntities) {
//      if (entity instanceof EntityTameableDragon) {
//        EntityTameableDragon dragon = (EntityTameableDragon) entity;
//        if (dragon.isOwner(event.player)) {
//          BreathWeaponTarget breathWeaponTarget = dragon.getBreathHelper().getPlayerSelectedTarget();
//          if (breathWeaponTarget != null) {
//            targetToHighlight = breathWeaponTarget;
//          }
//        }
//      }
//    }
//    BreathWeaponTarget targetBeingBreathedAt = DragonOrbControl.getInstance().getTarget();
//    if (targetBeingBreathedAt != null) {
//      targetToHighlight = targetBeingBreathedAt;
//    }

    switch (targetToHighlight.getTypeOfTarget()) {
      case ENTITY: {
        highlightEntity(targetToHighlight.getTargetEntity(event.player.getEntityWorld()), event.player, event.partialTicks);
        break;
      }
      case LOCATION: {
        highlightBlock(targetToHighlight.getTargetedLocation(), event.player, event.partialTicks);
        break;
      }
      case DIRECTION: {
        return;
      }
      default: {
        System.err.println("Unknown target type in blockHighlightDecider : " + targetToHighlight.getTypeOfTarget());
        return;
      }
    }
  }

  private void highlightEntity(Entity entity, EntityPlayer entityPlayer, double partialTick)
  {
    if (entity == null || entityPlayer == null) return;

    AxisAlignedBB entityAABB = entity.getEntityBoundingBox();

    int timeMS = (int) System.currentTimeMillis();
    drawAABB(entityAABB, entityPlayer, partialTick, timeMS, Color.RED);
  }

  private void highlightBlock(Vec3 worldPos, EntityPlayer entityPlayer, double partialTick)
  {
    if (worldPos == null || entityPlayer == null) return;
    BlockPos blockPos = new BlockPos(worldPos);
    AxisAlignedBB blockAABB = new AxisAlignedBB(blockPos, blockPos.add(1, 1, 1));

    int timeMS = (int)System.currentTimeMillis();
    drawAABB(blockAABB, entityPlayer, partialTick, timeMS, Color.RED);
  }

  /** draw an outlined bounding box around the indicated aabb
   *
   * @param aabb the aabb to draw
   * @param entityPlayer used to offset based on player's eye position
   * @param partialTick
   * @param animationTimerMS a timer used to updateFromAnimator the bounding box (expands & contracts). 0 = no effect.  Elapsed
   *                         time in milliseconds.
   */
  private void drawAABB(AxisAlignedBB aabb, EntityPlayer entityPlayer, double partialTick, int animationTimerMS,
                        Color colour)
  {
    final double EXPANSION_MIN = 0.002; // amount to expand the AABB by
    final double EXPANSION_MAX = 0.100;
    double expansionamount = EXPANSION_MIN;
    if (animationTimerMS != 0) {
      final int CYCLE_TIME_MS = 1000;
      double cyclePos = MathX.modulus(animationTimerMS, CYCLE_TIME_MS) / (double)CYCLE_TIME_MS;
      if (cyclePos < 0.5) {
        expansionamount = MathX.terp(EXPANSION_MIN, EXPANSION_MAX, cyclePos * 2.0);
      } else {
        expansionamount = MathX.terp(EXPANSION_MAX, EXPANSION_MIN, 2 * (cyclePos - 0.5));
      }
    }
    AxisAlignedBB expandedBox = aabb.expand(expansionamount, expansionamount, expansionamount);

    try {
      GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
      GL11.glEnable(GL11.GL_BLEND);
      OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
      final float ALPHA = 0.4F;
      GL11.glColor4ub((byte)colour.getRed(), (byte)colour.getGreen(), (byte)colour.getBlue(), (byte)(255 * ALPHA));
      GL11.glLineWidth(2.0F);
      GL11.glDisable(GL11.GL_TEXTURE_2D);
      GL11.glDepthMask(false);

      double px = entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * partialTick;
      double py = entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * partialTick;
      double pz = entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * partialTick;

      Color dummyDrawColour = Color.WHITE;
      RenderGlobal.drawOutlinedBoundingBox(expandedBox.offset(-px, -py, -pz), dummyDrawColour.getRGB());
    } finally {
      GL11.glPopAttrib();
    }

  }

}
