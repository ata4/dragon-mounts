package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
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
public class SoundEffectBreathWeapon
{
  public SoundEffectBreathWeapon(SoundController i_soundController, WeaponSoundUpdateLink i_weaponSoundUpdateLink)
  {
    soundController = i_soundController;
    startupResource = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_START.getJsonName());
    loopResource = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_LOOP.getJsonName());
    stopResource = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_STOP.getJsonName());
    weaponSoundUpdateLink = i_weaponSoundUpdateLink;
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
    currentWeaponState = WeaponSoundInfo.State.IDLE;
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
//    if (failSound != null) {
//      soundController.stopSound(failSound);
//      failSound = null;
//    }
  }

  private void setAllStopFlags()
  {
    if (chargeSound != null) { chargeSound.donePlaying = true;}
    if (chargeLoopSound != null) { chargeLoopSound.donePlaying = true;}
    if (performSound != null) { performSound.donePlaying = true;}
//    if (failSound != null) { failSound.donePlaying = true;}
  }

  public void performTick(EntityPlayerSP entityPlayerSP)
  {
    ++ticksElapsed;
    WeaponSoundInfo weaponSoundInfo = new WeaponSoundInfo();
    boolean keepPlaying = weaponSoundUpdateLink.refreshWeaponSoundInfo(weaponSoundInfo);
    if (!keepPlaying) {
      setAllStopFlags();
      return;
    }



    if (weaponSoundInfo.ringState != currentWeaponState) {
      switch (weaponSoundInfo.ringState) {
        case IDLE: {
          break;
        }
        case SPIN_UP: {
          if (chargeSound != null) {
//             chargeSound.donePlaying = true;
            soundController.stopSound(chargeSound);
          }
          chargeSound = new BreathWeaponSound(startupResource, CHARGE_MIN_VOLUME, RepeatType.NO_REPEAT, chargeSettings);
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
          performSound = new BreathWeaponSound(performingResource, PERFORM_MIN_VOLUME, RepeatType.REPEAT, performSettings);
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
          failSound = new BreathWeaponSound(stopResource, FAIL_MAX_VOLUME, RepeatType.NO_REPEAT, failSettings);
          soundController.playSound(failSound);
          failureStartTick = ticksElapsed;
          break;
        }
        default: {
          ErrorLog.defaultLog().debug("Illegal ringSoundInfo.ringState:" + weaponSoundInfo.ringState + " in " + this.getClass());
        }
      }
      currentWeaponState = weaponSoundInfo.ringState;
    }

    switch (currentWeaponState) {
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
          chargeLoopSound = new BreathWeaponSound(loopResource, CHARGE_MIN_VOLUME, RepeatType.REPEAT, chargeLoopSettings);
          soundController.playSound(chargeLoopSound);
        }

        if (ticksElapsed - powerupStartTick <= POWERUP_SOUND_DURATION_TICKS) {
          float newVolume = CHARGE_MIN_VOLUME + (CHARGE_MAX_VOLUME - CHARGE_MIN_VOLUME) * (ticksElapsed - powerupStartTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
          chargeSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, CHARGE_MIN_VOLUME, CHARGE_MAX_VOLUME);

          newVolume = PERFORM_MIN_VOLUME + (PERFORM_MAX_VOLUME - PERFORM_MIN_VOLUME) * (ticksElapsed - powerupStartTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
          performSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, PERFORM_MIN_VOLUME, PERFORM_MAX_VOLUME);
          chargeLoopSettings.masterVolume = 0.0F;
        } else if (ticksElapsed - powerupStartTick <= POWERUP_SOUND_DURATION_TICKS + POWERUP_VOLUME_CROSSFADE_TICKS) {
          int crossfadeTicks = ticksElapsed - powerupStartTick - POWERUP_SOUND_DURATION_TICKS;
          float newVolume = CHARGE_MIN_VOLUME + (CHARGE_MAX_VOLUME - CHARGE_MIN_VOLUME) * crossfadeTicks / (float)POWERUP_VOLUME_CROSSFADE_TICKS;
          chargeLoopSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, CHARGE_MIN_VOLUME, CHARGE_MAX_VOLUME);
          chargeSettings.masterVolume = CHARGE_MAX_VOLUME - chargeLoopSettings.masterVolume;
          performSettings.masterVolume = PERFORM_MAX_VOLUME;
        } else {
          chargeLoopSettings.masterVolume = CHARGE_MAX_VOLUME;
          performSettings.masterVolume = PERFORM_MAX_VOLUME;
          chargeSettings.masterVolume = 0;
        }

        final int PERFORM_VOLUME_FADEDOWN_TICKS = 5;
        if (currentWeaponState == WeaponSoundInfo.State.PERFORMING_ACTION) {
          performSettings.masterVolume = PERFORM_MAX_VOLUME;
          int crossfadeTime = ticksElapsed - performStartTick;
          if (crossfadeTime <= PERFORM_VOLUME_FADEDOWN_TICKS) {
            float newVolume = CHARGE_MAX_VOLUME / (float)PERFORM_VOLUME_FADEDOWN_TICKS;
            chargeSettings.masterVolume = newVolume;
            chargeLoopSettings.masterVolume = newVolume;
          } else {
            chargeLoopSettings.masterVolume = 0;
            chargeSettings.masterVolume = 0;
          }
        }
        break;
      }
      case SPIN_DOWN:
      case SPIN_UP_ABORT: {
        chargeSettings.masterVolume = 0;
        chargeLoopSettings.masterVolume = 0;

        final int ABORT_VOLUME_FADEDOWN_TICKS = 20;
        performSettings.masterVolume -= PERFORM_MAX_VOLUME / (float)ABORT_VOLUME_FADEDOWN_TICKS;
        if (performSettings.masterVolume < 0) {
          performSettings.masterVolume = 0;
          if (performSound != null) {
//            performSound.donePlaying = true;
            soundController.stopSound(performSound);
          }
        }
        break;
      }
      case FAILURE: {
        chargeSettings.masterVolume = 0;
        chargeLoopSettings.masterVolume = 0;
        failSettings.masterVolume = FAIL_MAX_VOLUME;

        final int FAILURE_VOLUME_FADEDOWN_TICKS = 20;
        performSettings.masterVolume -= PERFORM_MAX_VOLUME / (float)FAILURE_VOLUME_FADEDOWN_TICKS;
        if (performSettings.masterVolume < 0) {
          performSettings.masterVolume = 0;
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
//    public boolean updateSoundSettings(ComponentSoundSettings soundSettings)
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
  WeaponSoundInfo.State currentWeaponState = WeaponSoundInfo.State.IDLE;

  private ComponentSoundSettings chargeSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings chargeLoopSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings performSettings = new ComponentSoundSettings(0.01F);
  private ComponentSoundSettings failSettings = new ComponentSoundSettings(0.01F);

  private BreathWeaponSound chargeSound;
  private BreathWeaponSound chargeLoopSound;
  private BreathWeaponSound performSound;
  private BreathWeaponSound failSound;

  private SoundController soundController;
  private ResourceLocation startupResource;
  private ResourceLocation loopResource;
  private ResourceLocation stopResource;
  private ResourceLocation performingResource;

  private WeaponSoundUpdateLink weaponSoundUpdateLink;

  /**
   * Used as a callback to update the sound's position and
   */
  public interface WeaponSoundUpdateLink
  {
    public boolean refreshWeaponSoundInfo(WeaponSoundInfo infoToUpdate);
  }

  public static class WeaponSoundInfo
  {
    public enum State {IDLE, BREATHING}
    public State ringState = State.IDLE;
    public Vec3 location;
  }

  private static class ComponentSoundSettings
  {
    public ComponentSoundSettings(float i_volume)
    {
      masterVolume = i_volume;
    }
    public float masterVolume;
    public Vec3 soundEpicentre;
    public float playerDistanceToEpicentre;
    public boolean playing;
  }

  public enum RepeatType {REPEAT, NO_REPEAT}

  private class BreathWeaponSound extends PositionedSound implements ITickableSound
  {
    public BreathWeaponSound(ResourceLocation i_resourceLocation, float i_volume, RepeatType i_repeat,
                             ComponentSoundSettings i_soundSettings)
    {
      super(i_resourceLocation);
      repeat = (i_repeat == RepeatType.REPEAT);
      volume = i_volume;
      attenuationType = AttenuationType.NONE;
      soundSettings = i_soundSettings;
    }

    private boolean donePlaying;
    ComponentSoundSettings soundSettings;

    @Override
    public boolean isDonePlaying() {
      return donePlaying;
    }

    @Override
    public void update() {
      final float MINIMUM_VOLUME = 0.01F;
      final float MAXIMUM_VOLUME = 0.05F;
      final float INSIDE_VOLUME = 0.10F;
      final float OFF_VOLUME = 0.0F;
      if (!soundSettings.playing) {
//        donePlaying = true;
        this.volume = OFF_VOLUME;
      } else {
//        System.out.println(boundaryHumInfo.playerDistanceToEpicentre);
        this.xPosF = (float)soundSettings.soundEpicentre.xCoord;
        this.yPosF = (float)soundSettings.soundEpicentre.yCoord;
        this.zPosF = (float)soundSettings.soundEpicentre.zCoord;
        if (soundSettings.playerDistanceToEpicentre < 0.01F) {
          this.volume = INSIDE_VOLUME;
        } else {
          final float MINIMUM_VOLUME_DISTANCE = 20.0F;
          float fractionToMinimum = soundSettings.playerDistanceToEpicentre / MINIMUM_VOLUME_DISTANCE;
          this.volume = MathX.clamp(MAXIMUM_VOLUME - fractionToMinimum * (MAXIMUM_VOLUME - MINIMUM_VOLUME),
                                    MINIMUM_VOLUME, MAXIMUM_VOLUME);
        }
      }
    }
  }
}
