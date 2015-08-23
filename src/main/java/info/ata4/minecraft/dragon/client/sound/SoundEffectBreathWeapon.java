package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TheGreyGhost on 8/10/14.
 *
 * Used to create sound effects for the breath weapon tool - start up, sustained loop, and wind-down
 * The sound made by the dragon's head
 *   1) initial startup
 *   2) looping while breathing
 *   3) stopping when done
 *  Sometimes the sound doesn't layer properly on the first try.  I don't know why.
 *
 * The SoundEffectBreathWeapon corresponds to the breath weapon of a single dragon.  Typical usage is:
 * 1) create an instance, and provide a callback function (WeaponSoundUpdateLink)
 * 2) startPlaying(), startPlayingIfNotAlreadyPlaying(), stopPlaying() to start or stop the sounds completely
 * 3) once per tick, call performTick().
 *   3a) performTick() will call the WeaponSoundUpdateLink.refreshWeaponSoundInfo(), which should return the
 *       current data relevant to the sound (eg whether the dragon is breathing, and the location of the beam)
 *
 */
public class SoundEffectBreathWeapon
{
  public SoundEffectBreathWeapon(SoundController i_soundController, WeaponSoundUpdateLink i_weaponSoundUpdateLink)
  {
    soundController = i_soundController;
    headStartRL = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_HEAD_START.getJsonName());
    headLoopRL = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_HEAD_LOOP.getJsonName());
//    headLoopRL2 = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_HEAD_LOOP2.getJsonName());
    headStopRL = new ResourceLocation(SoundEffectNames.WEAPON_FIRE_HEAD_STOP.getJsonName());
    weaponSoundUpdateLink = i_weaponSoundUpdateLink;
  }

  private Random random = new Random();

  private final float HEAD_MIN_VOLUME = 0.02F;
  private final float HEAD_MAX_VOLUME = 0.2F;
  private final float PERFORM_MIN_VOLUME = 0.02F;
  private final float PERFORM_MAX_VOLUME = 0.2F;
  private final float FAIL_MIN_VOLUME = 0.02F;
  private final float FAIL_MAX_VOLUME = 0.2F;

  public void startPlaying(EntityPlayerSP entityPlayerSP)
  {
    stopAllSounds();
    currentWeaponState = WeaponSoundInfo.State.IDLE;
    performTick(entityPlayerSP);
  }

//  public void startPlayingIfNotAlreadyPlaying(EntityPlayerSP entityPlayerSP)
//  {
//    if (performSound != null && !performSound.isDonePlaying()) return;
//    startPlaying(entityPlayerSP);
//  }

  public void stopPlaying()
  {
    stopAllSounds();
  }

  private void stopAllSounds()
  {
    stopAllHeadSounds();
//    if (failSound != null) {
//      soundController.stopSound(failSound);
//      failSound = null;
//    }
  }

  private void stopAllHeadSounds()
  {
    if (headStartupSound != null) {
      soundController.stopSound(headStartupSound);
      headStartupSound = null;
    }
//    for (BreathWeaponSound sound : headLoopSounds) {
//      if (sound != null) {
//        soundController.stopSound(sound);
//      }
//    }
//    headLoopSounds.clear();
    if (headLoopSound != null) {
      soundController.stopSound(headLoopSound);
      headLoopSound = null;
    }

    if (headStoppingSound != null) {
      soundController.stopSound(headStoppingSound);
      headStoppingSound = null;
    }
  }


  private void setAllStopFlags()
  {
    if (headStartupSound != null) { headStartupSound.setDonePlaying();}
    if (headLoopSound != null) { headLoopSound.setDonePlaying();}
//    for (BreathWeaponSound sound : headLoopSounds) {
//      if (sound != null) {
//        sound.setDonePlaying();
//      }
//    }
    if (headStoppingSound != null) { headStoppingSound.setDonePlaying();}
  }

  /**
   * Updates all the component sounds according to the state of the breath weapon.
   * @param entityPlayerSP
   */
  public void performTick(EntityPlayerSP entityPlayerSP) {
    ++ticksElapsed;
    WeaponSoundInfo weaponSoundInfo = new WeaponSoundInfo();
    boolean keepPlaying = weaponSoundUpdateLink.refreshWeaponSoundInfo(weaponSoundInfo);
    if (!keepPlaying) {
      setAllStopFlags();
      return;
    }
    checkNotNull(weaponSoundInfo.dragonHeadLocation);
    headSoundSettings.playing = true;
    headSoundSettings.masterVolume = weaponSoundInfo.relativeVolume;
    headSoundSettings.soundEpicentre = weaponSoundInfo.dragonHeadLocation;

    headSoundSettings.playerDistanceToEpicentre =
              (float) weaponSoundInfo.dragonHeadLocation.distanceTo(entityPlayerSP.getPositionVector());

    final int HEAD_STARTUP_TICKS = 40;
    final int HEAD_STOPPING_TICKS = 60;

    //todo  player gets burned by fireballs even though the balls are nowhere near the player'

    // if state has changed, stop and start component sounds appropriately

    if (weaponSoundInfo.breathingState != currentWeaponState) {
      switch (weaponSoundInfo.breathingState) {
        case IDLE: {
//          breathingStopTick = ticksElapsed;
          stopAllHeadSounds();
          headStoppingSound = new BreathWeaponSound(headStopRL, HEAD_MIN_VOLUME, RepeatType.NO_REPEAT,
                                                    headSoundSettings);
          headStoppingSound.setPlayCountdown(HEAD_STOPPING_TICKS);
          soundController.playSound(headStoppingSound);
          break;
        }
        case BREATHING: {
//          breathingStartTick = ticksElapsed;
          stopAllHeadSounds();
          BreathWeaponSound preloadLoop = new BreathWeaponSound(headLoopRL, Mode.PRELOAD);
          soundController.playSound(preloadLoop);
          BreathWeaponSound preLoadStop = new BreathWeaponSound(headStopRL, Mode.PRELOAD);
          soundController.playSound(preLoadStop);
          headStartupSound = new BreathWeaponSound(headStartRL, HEAD_MIN_VOLUME, RepeatType.NO_REPEAT,
                                                   headSoundSettings);
          headStartupSound.setPlayCountdown(HEAD_STARTUP_TICKS);
          soundController.playSound(headStartupSound);
          break;
        }
        default: {
          System.err.printf("Illegal weaponSoundInfo.breathingState:" + weaponSoundInfo.breathingState + " in " + this
                  .getClass());
        }
      }
      currentWeaponState = weaponSoundInfo.breathingState;
    }

    // update component sound settings based on weapon info and elapsed time

    switch (currentWeaponState) {
      case BREATHING: {
        if (headStartupSound != null && headStartupSound.getPlayCountdown() <= 0) {
          stopAllHeadSounds();
//          headLoopSounds.clear();
//          headLoopSounds.add(new BreathWeaponSound(headLoopRL1, HEAD_MIN_VOLUME, RepeatType.REPEAT, headSoundSettings));
//          headLoopSounds.add(new BreathWeaponSound(headLoopRL2, HEAD_MIN_VOLUME, RepeatType.REPEAT, headSoundSettings));
          headLoopSound = new BreathWeaponSound(headLoopRL, HEAD_MIN_VOLUME, RepeatType.REPEAT, headSoundSettings);
          soundController.playSound(headLoopSound);
        }

//        final int HEAD_LOOP_MIN_TICKS = 40;
//        final int HEAD_LOOP_MAX_TICKS = 100;
//        for (int i = 0; i < headLoopSounds.size(); ++i) {
//          BreathWeaponSound headLoopSound = headLoopSounds.get(i);
//          if (headLoopSound != null && headLoopSound.getPlayCountdown() < 0) {
//            soundController.stopSound(headLoopSound);
//            ResourceLocation rl = (random.nextBoolean()) ? headLoopRL1 : headLoopRL2;
//            headLoopSound = new BreathWeaponSound(rl, HEAD_MIN_VOLUME, RepeatType.REPEAT, headSoundSettings);
//            int playDuration = HEAD_LOOP_MIN_TICKS + random.nextInt(HEAD_LOOP_MAX_TICKS - HEAD_LOOP_MIN_TICKS);
//            headLoopSound.setPlayCountdown(playDuration);
//            soundController.playSound(headLoopSound);
//            headLoopSounds.set(i, headLoopSound);
//          }
//        }
        break;
      }
      case IDLE: {
        if (headStoppingSound != null) {
          if (headStoppingSound.getPlayCountdown() <= 0) {   //|| !soundController.isSoundPlaying(headStoppingSound)) {  causes strange bug "channel null in method 'stop'"
            soundController.stopSound(headStoppingSound);
            headStoppingSound = null;
          }
        }
        break;
      }
      default: {
        System.err.printf("Unknown currentWeaponState:" + currentWeaponState);
      }
    }
  }

//        final int POWERUP_SOUND_DURATION_TICKS = 40;
//        final int POWERUP_VOLUME_RAMP_TICKS = 10;
//        final int POWERUP_VOLUME_CROSSFADE_TICKS = 10;
//
//        if (ticksElapsed - headStartupTick <= POWERUP_SOUND_DURATION_TICKS) {
//          float newVolume = HEAD_MIN_VOLUME + (HEAD_MAX_VOLUME - HEAD_MIN_VOLUME) * (ticksElapsed - headStartupTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
//          headStartSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, HEAD_MIN_VOLUME, HEAD_MAX_VOLUME);
//
//          newVolume = PERFORM_MIN_VOLUME + (PERFORM_MAX_VOLUME - PERFORM_MIN_VOLUME) * (ticksElapsed - headStartupTick) / (float)POWERUP_VOLUME_RAMP_TICKS;
//          headStoppingSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, PERFORM_MIN_VOLUME, PERFORM_MAX_VOLUME);
//          headLoopSettings.masterVolume = 0.0F;
//        } else if (ticksElapsed - headStartupTick <= POWERUP_SOUND_DURATION_TICKS + POWERUP_VOLUME_CROSSFADE_TICKS) {
//          int crossfadeTicks = ticksElapsed - headStartupTick - POWERUP_SOUND_DURATION_TICKS;
//          float newVolume = HEAD_MIN_VOLUME + (HEAD_MAX_VOLUME - HEAD_MIN_VOLUME) * crossfadeTicks / (float)POWERUP_VOLUME_CROSSFADE_TICKS;
//          headLoopSettings.masterVolume = UsefulFunctions.clipToRange(newVolume, HEAD_MIN_VOLUME, HEAD_MAX_VOLUME);
//          headStartSettings.masterVolume = HEAD_MAX_VOLUME - headLoopSettings.masterVolume;
//          headStoppingSettings.masterVolume = PERFORM_MAX_VOLUME;
//        } else {
//          headLoopSettings.masterVolume = HEAD_MAX_VOLUME;
//          headStoppingSettings.masterVolume = PERFORM_MAX_VOLUME;
//          headStartSettings.masterVolume = 0;
//        }
//
//        final int PERFORM_VOLUME_FADEDOWN_TICKS = 5;
//        if (currentWeaponState == WeaponSoundInfo.State.PERFORMING_ACTION) {
//          headStoppingSettings.masterVolume = PERFORM_MAX_VOLUME;
//          int crossfadeTime = ticksElapsed - performStartTick;
//          if (crossfadeTime <= PERFORM_VOLUME_FADEDOWN_TICKS) {
//            float newVolume = HEAD_MAX_VOLUME / (float)PERFORM_VOLUME_FADEDOWN_TICKS;
//            headStartSettings.masterVolume = newVolume;
//            headLoopSettings.masterVolume = newVolume;
//          } else {
//            headLoopSettings.masterVolume = 0;
//            headStartSettings.masterVolume = 0;
//          }
//        }
//        break;
//      }
//      case SPIN_DOWN:
//      case SPIN_UP_ABORT: {
//        headStartSettings.masterVolume = 0;
//        headLoopSettings.masterVolume = 0;
//
//        final int ABORT_VOLUME_FADEDOWN_TICKS = 20;
//        headStoppingSettings.masterVolume -= PERFORM_MAX_VOLUME / (float)ABORT_VOLUME_FADEDOWN_TICKS;
//        if (headStoppingSettings.masterVolume < 0) {
//          headStoppingSettings.masterVolume = 0;
//          if (performSound != null) {
////            performSound.donePlaying = true;
//            soundController.stopSound(performSound);
//          }
//        }
//        break;
//      }
//      case FAILURE: {
//        headStartSettings.masterVolume = 0;
//        headLoopSettings.masterVolume = 0;
//        failSettings.masterVolume = FAIL_MAX_VOLUME;
//
//        final int FAILURE_VOLUME_FADEDOWN_TICKS = 20;
//        headStoppingSettings.masterVolume -= PERFORM_MAX_VOLUME / (float)FAILURE_VOLUME_FADEDOWN_TICKS;
//        if (headStoppingSettings.masterVolume < 0) {
//          headStoppingSettings.masterVolume = 0;
//          if (performSound != null) {
////            performSound.donePlaying = true;
//            soundController.stopSound(performSound);
//          }
//        }
//        break;
//      }

  private int ticksElapsed;
  private int breathingStartTick;
  private int breathingStopTick;
  private int headStartupTick;
  private int performStartTick;
  private int performStopTick;
  private int failureStartTick;
  private int spinupAbortTick;
  WeaponSoundInfo.State currentWeaponState = WeaponSoundInfo.State.IDLE;

  private ComponentSoundSettings headSoundSettings = new ComponentSoundSettings(1.0F);
//  private ComponentSoundSettings headLoopSettings = new ComponentSoundSettings(0.01F);
//  private ComponentSoundSettings headStoppingSettings = new ComponentSoundSettings(0.01F);
//  private ComponentSoundSettings failSettings = new ComponentSoundSettings(0.01F);

  private BreathWeaponSound headStartupSound;
  private BreathWeaponSound headLoopSound;
//  private ArrayList<BreathWeaponSound> headLoopSounds = new ArrayList<BreathWeaponSound>();
  private BreathWeaponSound headStoppingSound;
//  private ArrayList<BreathWeaponSound> beamSounds;

  private SoundController soundController;
  private ResourceLocation headStartRL;
  private ResourceLocation headLoopRL;
//  private ResourceLocation headLoopRL1;
//  private ResourceLocation headLoopRL2;
  private ResourceLocation headStopRL;
//  private ResourceLocation beamLoopResource;

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
    public State breathingState = State.IDLE;
    public Collection<Vec3> pointsWithinBeam;
    public Vec3 dragonHeadLocation;
    public float relativeVolume; // 0 to 1
  }

  // settings for each component sound
  private static class ComponentSoundSettings
  {
    public ComponentSoundSettings(float i_volume)
    {
      masterVolume = i_volume;
    }
    public float masterVolume;  // multiplier for the volume = 0 .. 1
    public Vec3 soundEpicentre;
    public float playerDistanceToEpicentre;
    public boolean playing;
  }

  public enum RepeatType {REPEAT, NO_REPEAT}
  public enum Mode {PRELOAD, PLAY}

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
      playMode = Mode.PLAY;
    }

    /**
     * Preload for this sound (plays at very low volume).
     * Can't be a static method because that's not allowed in inner class
     * @param i_resourceLocation the sound to be played
     * @param mode dummy argument.  Must always be PRELOAD
     */
    public BreathWeaponSound(ResourceLocation i_resourceLocation, Mode mode)
    {
      super(i_resourceLocation);
      checkArgument(mode == Mode.PRELOAD);
      repeat = false;
      final float VERY_LOW_VOLUME = 0.001F;
      volume = VERY_LOW_VOLUME;
      attenuationType = AttenuationType.NONE;
      soundSettings = null;
      playMode = Mode.PRELOAD;
    }

    private void setDonePlaying() {
      donePlaying = true;
    }

    private boolean donePlaying;
    private ComponentSoundSettings soundSettings;
    private Mode playMode;

    public int getPlayCountdown() {
      return playTimeCountDown;
    }

    public void setPlayCountdown(int countdown) {
      playTimeCountDown = countdown;
    }

    private int playTimeCountDown = -1;


    @Override
    public boolean isDonePlaying() {
      return donePlaying;
    }

    @Override
    public void update() {
      final float MINIMUM_VOLUME = 0.10F;
      final float MAXIMUM_VOLUME = 1.00F;
      final float INSIDE_VOLUME = 1.00F;
      final float OFF_VOLUME = 0.0F;

      if (playMode == Mode.PRELOAD) {
//        this.volume = OFF_VOLUME;
        return;
      }

      --playTimeCountDown;
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
          final float MINIMUM_VOLUME_DISTANCE = 40.0F;
          float fractionToMinimum = soundSettings.playerDistanceToEpicentre / MINIMUM_VOLUME_DISTANCE;
          this.volume = soundSettings.masterVolume *
                        MathX.clamp(MAXIMUM_VOLUME - fractionToMinimum * (MAXIMUM_VOLUME - MINIMUM_VOLUME),
                                    MINIMUM_VOLUME, MAXIMUM_VOLUME);
        }
      }
    }
  }
}
