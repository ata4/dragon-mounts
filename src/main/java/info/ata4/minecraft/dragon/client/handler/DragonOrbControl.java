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
        import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
        import info.ata4.minecraft.dragon.server.network.DragonTargetMessage;
        import info.ata4.minecraft.dragon.server.util.ItemUtils;
        import info.ata4.minecraft.dragon.server.util.RayTraceServer;
        import net.minecraft.client.Minecraft;
        import net.minecraft.client.entity.EntityPlayerSP;
        import net.minecraft.util.*;
        import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
        import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
        import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

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
   * Send the target to the server at periodic intervals (if the target has changed significantly, or at least every x ticks)
   * @param evt
   */
  @SubscribeEvent
  public void onTick(ClientTickEvent evt) {
    if (evt.phase != ClientTickEvent.Phase.START) return;
    EntityPlayerSP entityPlayerSP = Minecraft.getMinecraft().thePlayer;
    if (entityPlayerSP == null) return;

    boolean oldTriggerHeld = triggerHeld;

    if (!ItemUtils.hasEquipped(entityPlayerSP, DragonMounts.proxy.itemDragonOrb)) {
      triggerHeld = false;
    } else {
      triggerHeld = entityPlayerSP.isUsingItem();
      if (triggerHeld) {
        final float MAX_ORB_RANGE = 20.0F;
        MovingObjectPosition mop = RayTraceServer.getMouseOver(entityPlayerSP.getEntityWorld(), entityPlayerSP, MAX_ORB_RANGE);
        breathWeaponTarget = BreathWeaponTarget.fromMovingObjectPosition(mop, entityPlayerSP);
      }
    }

    boolean needToSendMessage = false;
    if (!triggerHeld) {
      needToSendMessage = oldTriggerHeld;
    } else {
      if (!oldTriggerHeld) {
        needToSendMessage = true;
      } else {
        needToSendMessage = !breathWeaponTarget.approximatelyMatches(lastTargetSent);
      }
    }

    ++ticksSinceLastMessage;
    if (ticksSinceLastMessage >= MAX_TIME_NO_MESSAGE) {
      needToSendMessage = true;
    }

    if (needToSendMessage) {
      ticksSinceLastMessage = 0;
      lastTargetSent = breathWeaponTarget;
      DragonTargetMessage message = null;
      if (triggerHeld) {
        message = DragonTargetMessage.createTargetMessage(breathWeaponTarget);
      } else {
        message = DragonTargetMessage.createUntargetMessage();
      }
      network.sendToServer(message);
    }
  }

  private final int MAX_TIME_NO_MESSAGE = 20;  // send a message at least this many ticks or less
  private int ticksSinceLastMessage = 0;
  /**
   * Get the block or entity being targeted by the dragon orb
   * @return BreathWeaponTarget, or null for no target
   */
  public BreathWeaponTarget getTarget()
  {
    if (triggerHeld) {
      return breathWeaponTarget;
    } else {
      return null;
    }
  }

  private boolean triggerHeld = false;
  private BreathWeaponTarget breathWeaponTarget;
  private BreathWeaponTarget lastTargetSent;

  private static DragonOrbControl instance = null;

  private DragonOrbControl(SimpleNetworkWrapper i_network) {
    network = i_network;
    lastTargetSent = null;
  }
}
