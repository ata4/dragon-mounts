
package info.ata4.minecraft.dragon.client.handler;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.userinput.KeyBindingInterceptor;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.network.DragonTargetMessage;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import info.ata4.minecraft.dragon.server.util.RayTraceServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * This event handler is used to capture player input while the player is holding the dragon orb:
 * If the player is holding the dragon orb, records whether the player is holding down the
 *   trigger and what the current target is (where the player is pointing the cursor)
 * Transmits this information to the server.
 * If the AutoLock option is selected, the orb will lock on to a target for as long as the trigger is held down.
 * If the autolock option isn't selected, the orb will change to whatever target is currently being looked at
 *
 * Usage:
 * SETUP
 * (1) Register a server-side message handler for DragonTargetMessage
 * (2) Create the singleton in PostInit (client only) using DragonOrbControl.createSingleton(getNetwork());
 * (3) Initialise the keypress interception in PostInit (client only) using DragonOrbControl.initialiseInterceptors();
 * (4) Register the handler in PostInit(client only) using FMLCommonHandler.instance().bus()
 *        .register(DragonOrbControl.getInstance());
 *
 * POLLING
 * (1) get the singleton instance using getInstance()
 * (2) getTargetBeingLookedAt() returns the target being looked at, regardless of whether the trigger is held or not, and
 *       regardless of whether there is an autolock target
 * (3) getTarget() returns the target of the orb while the trigger is being held.
 * (4) getTargetLockedOn() returns the target being breathed at (may be different to getTarget() if autolock is on).
 *     Client side only.
 *
 */

/*
 ** 2013 October 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
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
   * Every tick, check if the player is holding the Dragon Orb, and if so, whether the player is targeting something with it
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
      enableClickInterception(false);
      triggerHeld = false;
      targetBeingLookedAt = null;
    } else {
      enableClickInterception(true);
      final float MAX_ORB_RANGE = 20.0F;
      MovingObjectPosition mop = RayTraceServer.getMouseOver(entityPlayerSP.getEntityWorld(), entityPlayerSP, MAX_ORB_RANGE);
      targetBeingLookedAt = BreathWeaponTarget.fromMovingObjectPosition(mop, entityPlayerSP);
      triggerHeld = attackButtonInterceptor.isUnderlyingKeyDown();
      if (triggerHeld) {
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

    // if autolock is on, only change target when the player releases the button
    // (used on client side only, for rendering)  Server side AI is used for the real autolock
    boolean orbTargetAutoLock = DragonMounts.instance.getConfig().isOrbTargetAutoLock();
    if (breathWeaponTarget != null && triggerHeld) {
      if (!orbTargetAutoLock || targetLockedOn == null) {
        targetLockedOn = breathWeaponTarget;
      }
    } else {
      targetLockedOn = null;
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

  /**
   * Get the block or entity that the dragon orb cursor is currently pointing at
   * @return BreathWeaponTarget, or null for none
   */
  public BreathWeaponTarget getTargetBeingLookedAt()
  {
    return targetBeingLookedAt;
  }

  public BreathWeaponTarget getTargetLockedOn()
  {
    return targetLockedOn;
  }

  private boolean triggerHeld = false;
  private BreathWeaponTarget breathWeaponTarget;
  private BreathWeaponTarget lastTargetSent;
  private BreathWeaponTarget targetBeingLookedAt;
  private BreathWeaponTarget targetLockedOn;  // used client side only, for rendering.  server-side lockon is in AI

  private static DragonOrbControl instance = null;

  private DragonOrbControl(SimpleNetworkWrapper i_network) {
    network = i_network;
    lastTargetSent = null;
  }

  private static KeyBindingInterceptor attackButtonInterceptor;
  private static KeyBindingInterceptor useItemButtonInterceptor;

  public static void initialiseInterceptors()
  {
    attackButtonInterceptor = new KeyBindingInterceptor(Minecraft.getMinecraft().gameSettings.keyBindAttack);
    Minecraft.getMinecraft().gameSettings.keyBindAttack = attackButtonInterceptor;
    attackButtonInterceptor.setInterceptionActive(false);

//    useItemButtonInterceptor = new KeyBindingInterceptor(Minecraft.getMinecraft().gameSettings.keyBindUseItem);
//    Minecraft.getMinecraft().gameSettings.keyBindUseItem = useItemButtonInterceptor;
//    useItemButtonInterceptor.setInterceptionActive(false);
  }

  public static void enableClickInterception(boolean interception)
  {
//    useItemButtonInterceptor.setInterceptionActive(interception);
    attackButtonInterceptor.setInterceptionActive(interception);
  }

}
