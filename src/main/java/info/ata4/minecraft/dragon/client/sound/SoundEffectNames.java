package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.DragonMounts;

/**
 * User: The Grey Ghost
 * Date: 17/04/2014
 * Contains (some of the) sound effect names used for the dragon
 */
public enum SoundEffectNames
{
  WEAPON_FIRE_HEAD_START("mob.enderdragon.weaponfireheadstart"),
  WEAPON_FIRE_HEAD_LOOP("mob.enderdragon.weaponfireheadloop"),
//  WEAPON_FIRE_HEAD_LOOP2("mob.enderdragon.weaponfireheadloop2"),
  WEAPON_FIRE_HEAD_STOP("mob.enderdragon.weaponfireheadstop");

  public final String getJsonName() {return DragonMounts.AID + ":" + jsonName;}

  private SoundEffectNames(String i_jsonName) {
    jsonName = i_jsonName;
  }
  private final String jsonName;
}
