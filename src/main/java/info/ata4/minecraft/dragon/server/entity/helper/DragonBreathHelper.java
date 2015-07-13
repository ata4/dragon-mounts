package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.client.render.BreathWeaponEmitter;
import info.ata4.minecraft.dragon.client.render.FlameBreathFX;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.network.DragonOrbTargets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by TGG on 8/07/2015.
 * Responsible for
 * - retrieving the player's selected target (based on player's input from Dragon Orb item)
 * - synchronising the player-selected target between server AI and client copy - using datawatcher
 * - rendering the breath weapon on the client
 * - performing the effects of the weapon on the server (eg burning blocks, causing damage)
 * The selection of an actual target (typically - based on the player desired target), navigation of dragon to the appropriate range,
 *   turning the dragon to face the target, is done by targeting AI.
 * DragonBreathHelper is also responsible for
 *  - adding delays for jaw open / breathing start
 *  - interrupting the beam when the dragon is facing the wrong way / the angle of the beam mismatches the head angle
 *  Usage:
 *  1) Create instance, providing the parent dragon entity and a datawatcher index to use for breathing
 *  2) call onLivingUpdate(), onDeath(), onDeathUpdate(), readFromNBT() and writeFromNBT() from the corresponding
 *     parent entity methods
 *  3a) The AI task responsible for targeting should call getPlayerSelectedTarget() to find out what the player wants
 *     the dragon to target.
 *  3b) Once the target is in range and the dragon is facing the correct direction, the AI should use setBreathingTarget()
 *      to commence breathing at the target
 *  4) getCurrentBreathState() and getBreathStateFractionComplete() should be called by animation routines for
 *     the dragon during breath weapon (eg jaw opening)
 */
public class DragonBreathHelper extends DragonHelper
{
  private static final Logger L = LogManager.getLogger();

  public DragonBreathHelper(EntityTameableDragon dragon, int dataWatcherIndexBreathTarget)
  {
    super(dragon);
    DATA_WATCHER_BREATH_TARGET = dataWatcherIndexBreathTarget;
    dataWatcher.addObject(DATA_WATCHER_BREATH_TARGET, "");
    if (dragon.isClient()) {
      breathWeaponEmitter = new BreathWeaponEmitter();
    }
  }

  private final int DATA_WATCHER_BREATH_TARGET;

  private BreathWeaponTarget targetBeingBreathedAt = null;  // server: the target currently being breathed at
  private BreathWeaponTarget lastBreathTargetSent = null;   // server: the last target sent to the client thru DataWatcher

  private BreathState currentBreathState = BreathState.IDLE;
  private int transitionStartTick;

  private BreathWeaponEmitter breathWeaponEmitter = null;
  private int tickCounter = 0;

  public BreathState getCurrentBreathState() {return currentBreathState;}

  public float getBreathStateFractionComplete() {
    switch (currentBreathState) {
      case IDLE: {
        return 0.0F;
      }
      case STARTING: {
        int ticksSpentStarting = tickCounter - transitionStartTick;
        return MathHelper.clamp_float(ticksSpentStarting / (float)BREATH_START_DURATION, 0.0F, 1.0F);
      }
      case SUSTAIN: {
        return 0.0F;
      }
      case STOPPING: {
        int ticksSpentStopping = tickCounter - transitionStartTick;
        return MathHelper.clamp_float(ticksSpentStopping / (float)BREATH_STOP_DURATION, 0.0F, 1.0F);
      }
      default: {
        System.err.println("Unknown currentBreathState:" + currentBreathState);
        return 0.0F;
      }
    }
  }

  /** set the target currently being breathed at.
   * server only.
   * @param target the new target the dragon is breathing at, null = no target
    */
  public void setBreathingTarget(BreathWeaponTarget target)
  {
    if (dragon.isServer()) {
      targetBeingBreathedAt = target;
      boolean updateDataWatcher = false;
      if (lastBreathTargetSent == null) {
        updateDataWatcher = true;
      } else {
        updateDataWatcher = !lastBreathTargetSent.approximatelyMatches(target);
      }
      if (updateDataWatcher) {
        lastBreathTargetSent = target;
        if (target == null) {
          dataWatcher.updateObject(DATA_WATCHER_BREATH_TARGET, "");
        } else {
          dataWatcher.updateObject(DATA_WATCHER_BREATH_TARGET, target.toEncodedString());
        }
      }
    } else {
      L.warn("setBreathingTarget is only valid on server");
    }

    updateBreathState(target);
  }

  public enum  BreathState {
    IDLE, STARTING, SUSTAIN, STOPPING;
    }

  private final int BREATH_START_DURATION = 5; // ticks
  private final int BREATH_STOP_DURATION = 5; // ticks

  private void updateBreathState(BreathWeaponTarget targetBeingBreathedAt)
  {
    switch (currentBreathState) {
      case IDLE: {
        if (targetBeingBreathedAt != null) {
          transitionStartTick = tickCounter;
          currentBreathState = BreathState.STARTING;
        }
        break;
      }
      case STARTING: {
        int ticksSpentStarting = tickCounter - transitionStartTick;
        if (ticksSpentStarting >= BREATH_START_DURATION) {
          transitionStartTick = tickCounter;
          currentBreathState = (targetBeingBreathedAt != null) ? BreathState.SUSTAIN : BreathState.STOPPING;
        }
        break;
      }
      case SUSTAIN: {
        if (targetBeingBreathedAt == null) {
          transitionStartTick = tickCounter;
          currentBreathState = BreathState.STOPPING;
        }
        break;
      }
      case STOPPING: {
        int ticksSpentStopping = tickCounter - transitionStartTick;
        if (ticksSpentStopping >= BREATH_STOP_DURATION) {
          currentBreathState = BreathState.IDLE;
        }
        break;
      }
      default: {
        System.err.println("Unknown currentBreathState:" + currentBreathState);
        return;
      }
    }
  }

//  /**
//   * sets the breath weapon target selected by the player.  Will be used by the targeting AI to determine where to point
//   *   the breath weapon
//   * @param newPlayerSelectedTarget the target that the player wants the dragon to breathe at. null = no target
//   */
//  private void setPlayerSelectedTarget(BreathWeaponTarget newPlayerSelectedTarget)
//  {
//    if (newPlayerSelectedTarget == null) {
//      clearPlayerSelectedTarget();
//      return;
//    }
//    if (dragon.isClient()) {
//
//      playerSelectedTarget = newPlayerSelectedTarget;
//      return;
//    }
//    playerSelectedTarget = newPlayerSelectedTarget;
//    boolean updateDataWatcher = false;
//    if (lastBreathTargetSent == null) {
//      updateDataWatcher = true;
//    } else {
//      updateDataWatcher = !newPlayerSelectedTarget.approximatelyMatches(lastBreathTargetSent);
//    }
//    if (updateDataWatcher) {
//      dataWatcher.updateObject(DATA_WATCHER_BREATH_TARGET, playerSelectedTarget.toEncodedString());
//    }
//  }

//  /**
//   * clears the target for the breath weapon, i.e. no desired target
//   */
//  private void clearPlayerSelectedTarget()
//  {
//    playerSelectedTarget = null;
//    if (dragon.isClient() || lastBreathTargetSent == null) return;
//    dataWatcher.updateObject(DATA_WATCHER_BREATH_TARGET, "");
//  }

  /**
   * For tamed dragons, returns the target that their controlling player has selected using the DragonOrb.
   * Valid on server side only
   * @return the player's selected target, or null if no player target or dragon isn't tamed.
   */
  public BreathWeaponTarget getPlayerSelectedTarget()
  {
    if (dragon.isClient()) {
      L.warn("getPlayerSelectedTarget is valid on the server side only");
      return null;
    }

    Entity owner = dragon.getOwner();
    if (owner == null || !(owner instanceof EntityPlayerMP)) {
      return null;
    }
    EntityPlayerMP entityPlayerMP = (EntityPlayerMP)owner;
    BreathWeaponTarget breathWeaponTarget = DragonOrbTargets.getInstance().getPlayerTarget(entityPlayerMP);
    return breathWeaponTarget;
  }


  @Override
  public void onLivingUpdate() {
    ++tickCounter;
    if (dragon.isClient()) {
      onLivingUpdateClient();
    } else {
      onLivingUpdateServer();
    }
  }

  private void onLivingUpdateServer()
  {
    BreathWeaponTarget target = getTarget();
  }

  private void onLivingUpdateClient()
  {
    BreathWeaponTarget breathWeaponTarget = getTarget();
    updateBreathState(breathWeaponTarget);

    if (breathWeaponTarget != null) {
      Vec3 origin = dragon.getDragonHeadPositionHelper().getThroatPosition();
      Vec3 destination = breathWeaponTarget.getTargetedPoint(dragon.worldObj, origin);
      if (destination != null && currentBreathState == BreathState.SUSTAIN) {
        breathWeaponEmitter.setBeamEndpoints(origin, destination);
        FlameBreathFX.Power power = dragon.getLifeStageHelper().getBreathPower();
        breathWeaponEmitter.spawnBreathParticles(dragon.getEntityWorld(), power, tickCounter);
      }
    }
  }

  /**
   * Get the target currently being breathed at, for this dragon:
   * 1) On the client, from the datawatcher
   * 2) On the server- previously set by AI
   * @return the target, or null for none
   */
  private BreathWeaponTarget getTarget()
  {
    if (dragon.isClient()) {
      String target = dataWatcher.getWatchableObjectString(DATA_WATCHER_BREATH_TARGET);
      BreathWeaponTarget breathWeaponTarget = BreathWeaponTarget.fromEncodedString(target);
      return breathWeaponTarget;
    } else {
      return targetBeingBreathedAt;
    }
  }

  @Override
  public void writeToNBT(NBTTagCompound nbt) {}
  @Override
  public void readFromNBT(NBTTagCompound nbt) {}
  @Override
  public void applyEntityAttributes() {}
  @Override
  public void onDeathUpdate() {}
  @Override
  public void onDeath() {}

}
