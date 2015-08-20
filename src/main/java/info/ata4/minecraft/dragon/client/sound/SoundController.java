package info.ata4.minecraft.dragon.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* User: The Grey Ghost
* Date: 17/04/2014
*/
public class SoundController
{
  public void playSound(PositionedSound sound)
  {
    Minecraft.getMinecraft().getSoundHandler().playSound(sound);
  }

  public void playSound(PositionedSound sound, SoundEffectTickLink soundEffectTickLink)
  {
    Minecraft.getMinecraft().getSoundHandler().playSound(sound);
    soundEffectsToTick.put(sound, soundEffectTickLink);
  }

  public void stopSound(PositionedSound sound)
  {
    Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
    soundEffectsToTick.remove(sound);
  }

  /** tick all the sounds that need it
   */
  public void onTick()
  {
    Iterator<Map.Entry<PositionedSound, SoundEffectTickLink>> tickEntry = soundEffectsToTick.entrySet().iterator();
    while (tickEntry.hasNext()) {
      Map.Entry<PositionedSound, SoundEffectTickLink> entry = tickEntry.next();
      PositionedSound sound = entry.getKey();
      SoundEffectTickLink link = entry.getValue();
      boolean keepTicking = link.onTick( Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound));
      if (!keepTicking) {
        tickEntry.remove();
      }
    }
  }

  /**
   * The tick link is used by the controller to tick a sound
   */
  public interface SoundEffectTickLink {
    /**
     * called by the controller every tick
     * @param stillPlaying true if the sound is still playing
     * @return true if the sound wants to keep ticking, false to stop
     */
    public boolean onTick(boolean stillPlaying);
  }

  private HashMap<PositionedSound, SoundEffectTickLink> soundEffectsToTick = new HashMap<PositionedSound, SoundEffectTickLink>();

//  public abstract class SoundControlLink
//  {
//    public abstract void startSound();
//    public abstract void stopSound();
//  }
//
//  private class SoundControlLinkDoNothing extends SoundControlLink
//  {
//    public  void startSound() {};
//    public  void stopSound() {};
//  }
}
