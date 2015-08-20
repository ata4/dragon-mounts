package info.ata4.minecraft.dragon.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import speedytools.common.utilities.ErrorLog;
import speedytools.common.utilities.UsefulFunctions;

/**
 * Created by TheGreyGhost on 8/10/14.
 *
 * Used to create sound effects for the breath weapon tool - start up, sustained loop, and wind-down
 *
 * There are several basic states created from a couple of overlaid sounds
 * 1) initial startup
 * 2) looping while breathing
 * 3) stopping when done
 * performTick() should be called every tick by the client
 */
public class SoundEffectComplexTool
{
  public SoundEffectComplexTool(SoundController i_soundController, RingSoundUpdateLink i_ringSoundUpdateLink)
  {
    soundController = i_soundController;
    stopResource = new ResourceLocation(SoundEffectNames.POWERUP_FAILURE.getJsonName());
    loopResource = new ResourceLocation(SoundEffectNames.POWERUP_HOLD.getJsonName());
    startupResource = new ResourceLocation(SoundEffectNames.POWERUP.getJsonName());
    performingResource = new ResourceLocation(SoundEffectNames.PERFORMING_ACTION.getJsonName());
    ringSoundUpdateLink = i_ringSoundUpdateLink;
  }

  private final float CHARGE_MIN_VOLUME = 0.02F;
  private final float CHARGE_MAX_VOLUME = 0.2F;
  private final float PERFORM_MIN_VOLUME = 0.02F;
  private final float PERFORM_MAX_VOLUME = 0.2F;
  private final float FAIL_MIN_VOLUME = 0.02F;
  private final float FAIL_MAX_VOLUME = 0.2F;

  public void startPlaying()
  {
    stopAllSounds();
//    chargeSound = new RingSpinSound(startupResource, CHARGE_MIN_VOLUME, false, );
    currentRingState = RingSoundInfo.State.IDLE;
    performTick();
  }

  public void startPlayingIfNotAlreadyPlaying()
  {
    if (performSound != null && !performSound.isDonePlaying()) return;
    startPlaying();
  }

  public void stopPlaying()
  {
    stopAllSounds();
  }

  private void stopAllSounds()
  {
    if (chargeSound != null) {
      soundController.stopSound(chargeSound);
      chargeSound = null;
    }
    if (chargeLoopSound != null) {
      soundController.stopSound(chargeLoopSound);
      chargeLoopSound = null;
    }
    if (performSound != null) {
      soundController.stopSound(performSound);
      performSound = null;
    }
    if (failSound != null) {
      soundController.stopSound(failSound);
      failSound = null;
    }
  }

  private void setAllStopFlags()
  {
    if (chargeSound != null) { chargeSound.donePlaying = true;}
    if (chargeLoopSound != null) { chargeLoopSound.donePlaying = true;}
    if (performSound != null) { performSound.donePlaying = true;}
    if (failSound != null) { failSound.donePlaying = true;}
  }

  public void performTick()
  {
    ++ticksElapsed;
    RingSoundInfo ringSoundInfo = new RingSoundInfo();
    boolean keepPlaying = ringSoundUpdateLink.refreshRingSoundInfo(ringSoundInfo);
    if (!keepPlaying) {
      setAllStopFlags();
      return;
    }
    if (ringSoundInfo.ringState != currentRingState) {
      switch (ringSoundInfo.ringState) {
        case IDLE: {
          break;
        }
        case SPIN_UP: {
          if (chargeSound != null) {
//             chargeSound.donePlaying = true;
            soundController.stopSound(chargeSound);
          }
          chargeSound = new RingSpinSound(startupResource, CHARGE_MIN_VOLUME, RepeatType.NO_REPEAT, chargeSettings);
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
          performSound = new RingSpinSound(performingResource, PERFORM_MIN_VOLUME, RepeatType.REPEAT, performSettings);
          powerupStartTick = ticksElapsed;
          soundController.playSound(chargeSound);
          soundController.playSound(performSound);
          break;
        }
        case SPIN_UP_ABORT: {
          if (chargeSound != null) {
//            chargeSound.donePlaying = true;
            soundController.stopSound(chargeSound);
          }
          spinupAbortTick = ticksElapsed;
          break;
        }
        case PERFORMING_ACTION: {
          performStartTick = ticksElapsed;
          break;
        }
        case SPIN_DOWN: {
          performStopTick = ticksElapsed;
          break;
        }
        case FAILURE: {
          if (chargeSound != null) {
//            chargeSound.donePlaying = true;
            soundController.stopSound(chargeSound);
          }
          if (failSound != null) {
//            failSound.donePlaying = true;
            soundController.stopSound(failSound);
          }
          failSound = new RingSpinSound(stopResource, FAIL_MAX_VOLUME, RepeatType.NO_REPEAT, failSettings);
          soundController.playSound(failSound);
          failureStartTick = ticksElapsed;
          break;
        }
        default: {
          ErrorLog.defaultLog().debug("Illegal ringSoundInfo.ringState:" + ringSoundInfo.ringState + " in " + this.getClass());
        }
      }
      currentRingState = ringSoundInfo.ringState;
    }

    switch (currentRingState) {
      case SPIN_UP:
      case PERFORMING_ACTION: {
        final int POWERUP_SOUND_DURATION_TICKS = 40;
        final int POWERUP_VOLUME_RAMP_TICKS = 10;
        final int POWERUP_VOLUME_CROSSFADE_TICKS = 10;
        if (ticksElapsed - powerupStartTick == POWERUP_SOUND_DURATION_TICKS) {
          if (chargeLoopSound != null) {
//            chargeLoopSound.donePlaying = true;
            soundController.stopSound(chargeLoopSound);
          }
          chargeLoopSound = new RingSpinSound(loopResource, CHARGE_MIN_VOLUME, RepeatType.REPEAT, chargeLoopSettings);
          soundController.playSound(chargeLoopSound);
        }

        if (ticksElapsed - powerupStartTick <= POWERUP_SOUND_DURATION_TICKS) {
          float newVolume = CHARGE_MIN_VOLUME + (CHARGE_MAX_VOLUME - CHARGE_MIN_VOLUME) * (ticksElapsed - powerupStartTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
          chargeSettings.volume = UsefulFunctions.clipToRange(newVolume, CHARGE_MIN_VOLUME, CHARGE_MAX_VOLUME);

          newVolume = PERFORM_MIN_VOLUME + (PERFORM_MAX_VOLUME - PERFORM_MIN_VOLUME) * (ticksElapsed - powerupStartTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
          performSettings.volume = UsefulFunctions.clipToRange(newVolume, PERFORM_MIN_VOLUME, PERFORM_MAX_VOLUME);
          chargeLoopSettings.volume = 0.0F;
        } else if (ticksElapsed - powerupStartTick <= POWERUP_SOUND_DURATION_TICKS + POWERUP_VOLUME_CROSSFADE_TICKS) {
          int crossfadeTicks = ticksElapsed - powerupStartTick - POWERUP_SOUND_DURATION_TICKS;
          float newVolume = CHARGE_MIN_VOLUME + (CHARGE_MAX_VOLUME - CHARGE_MIN_VOLUME) * crossfadeTicks / (float)POWERUP_VOLUME_CROSSFADE_TICKS;
          chargeLoopSettings.volume = UsefulFunctions.clipToRange(newVolume, CHARGE_MIN_VOLUME, CHARGE_MAX_VOLUME);
          chargeSettings.volume = CHARGE_MAX_VOLUME - chargeLoopSettings.volume;
          performSettings.volume = PERFORM_MAX_VOLUME;
        } else {
          chargeLoopSettings.volume = CHARGE_MAX_VOLUME;
          performSettings.volume = PERFORM_MAX_VOLUME;
          chargeSettings.volume = 0;
        }

        final int PERFORM_VOLUME_FADEDOWN_TICKS = 5;
        if (currentRingState == RingSoundInfo.State.PERFORMING_ACTION) {
          performSettings.volume = PERFORM_MAX_VOLUME;
          int crossfadeTime = ticksElapsed - performStartTick;
          if (crossfadeTime <= PERFORM_VOLUME_FADEDOWN_TICKS) {
            float newVolume = CHARGE_MAX_VOLUME / (float)PERFORM_VOLUME_FADEDOWN_TICKS;
            chargeSettings.volume = newVolume;
            chargeLoopSettings.volume = newVolume;
          } else {
            chargeLoopSettings.volume = 0;
            chargeSettings.volume = 0;
          }
        }
        break;
      }
      case SPIN_DOWN:
      case SPIN_UP_ABORT: {
        chargeSettings.volume = 0;
        chargeLoopSettings.volume = 0;

        final int ABORT_VOLUME_FADEDOWN_TICKS = 20;
        performSettings.volume -= PERFORM_MAX_VOLUME / (float)ABORT_VOLUME_FADEDOWN_TICKS;
        if (performSettings.volume < 0) {
          performSettings.volume = 0;
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
        }
        break;
      }
      case FAILURE: {
        chargeSettings.volume = 0;
        chargeLoopSettings.volume = 0;
        failSettings.volume = FAIL_MAX_VOLUME;

        final int FAILURE_VOLUME_FADEDOWN_TICKS = 20;
        performSettings.volume -= PERFORM_MAX_VOLUME / (float)FAILURE_VOLUME_FADEDOWN_TICKS;
        if (performSettings.volume < 0) {
          performSettings.volume = 0;
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
        }
        break;
      }
      case IDLE: {
//        performSound.donePlaying = true;
        if (performSound != null) {
          soundController.stopSound(performSound);
          performSound = null;
        }
        break;
      }
    }

  }

//  private class PowerUpSoundUpdate implements RingSpinSoundUpdateMethod
//  {
//    public boolean updateSoundSettings(ComponentSoundSettings componentSoundSettings)
//    {
//      return false;
//    }
//  }

  private int ticksElapsed;
  private int powerupStartTick;
  private int performStartTick;
  private int performStopTick;
  private int failureStartTick;
  private int spinupAbortTick;
  RingSoundInfo.State currentRingState = RingSoundInfo.State.IDLE;

  private ComponentSoundSettings chargeSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings chargeLoopSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings performSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings failSettings = new ComponentSoundSettings(0.01F);

  private RingSpinSound chargeSound;
  private RingSpinSound chargeLoopSound;
  private RingSpinSound performSound;
  private RingSpinSound failSound;

  private SoundController soundController;
  private ResourceLocation startupResource;
  private ResourceLocation loopResource;
  private ResourceLocation stopResource;
  private ResourceLocation performingResource;

  private RingSoundUpdateLink ringSoundUpdateLink;

  /**
   * Used as a callback to update the sound's position and
   */
  public interface RingSoundUpdateLink
  {
    public boolean refreshRingSoundInfo(RingSoundInfo infoToUpdate);
  }

  public static class RingSoundInfo
  {
    public enum State {IDLE, SPIN_UP, SPIN_UP_ABORT, PERFORMING_ACTION, SPIN_DOWN, FAILURE}
    public State ringState = State.IDLE;
  }

//  private interface RingSpinSoundUpdateMethod
//  {
//    public boolean updateSoundSettings(ComponentSoundSettings componentSoundSettings);
//  }

  private static class ComponentSoundSettings
  {
    public ComponentSoundSettings(float i_volume)
    {
      volume = i_volume;
    }
    public float volume;
  }

  public enum RepeatType {REPEAT, NO_REPEAT}

  private class RingSpinSound extends PositionedSound implements ITickableSound
  {
    public RingSpinSound(ResourceLocation i_resourceLocation, float i_volume, RepeatType i_repeat, ComponentSoundSettings i_soundSettings)
    {
      super(i_resourceLocation);
      repeat = (i_repeat == RepeatType.REPEAT);
      volume = i_volume;
      attenuationType = AttenuationType.NONE;
      componentSoundSettings = i_soundSettings;
    }

    private boolean donePlaying;
    ComponentSoundSettings componentSoundSettings;

    @Override
    public boolean isDonePlaying() {
      return donePlaying;
    }

    @Override
    public void update() {
      this.volume = componentSoundSettings.volume;
    }
  }
}
