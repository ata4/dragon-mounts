package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.DragonMounts;

/**
 * User: The Grey Ghost
 * Date: 17/04/2014
 * Contains (some of the) sound effect names used for the dragon
 */
public enum SoundEffectNames
{
  WEAPON_FIRE_START("mob.enderdragon.weaponfirestart"),
  WEAPON_FIRE_LOOP("mob.enderdragon.weaponfireloop"),
  ;

  public final String getJsonName() {return DragonMounts.AID + ":" + jsonName;}

  private SoundEffectNames(String i_jsonName) {
    jsonName = i_jsonName;
  }
  private final String jsonName;
}
