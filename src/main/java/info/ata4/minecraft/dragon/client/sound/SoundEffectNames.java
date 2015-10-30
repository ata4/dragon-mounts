package info.ata4.minecraft.dragon.client.sound;

import info.ata4.minecraft.dragon.DragonMounts;

/**
 * User: The Grey Ghost
 * Date: 17/04/2014
 * Contains (some of the) sound effect names used for the dragon
 */
public enum SoundEffectNames
{
  ADULT_BREATHE_FIRE_START("mob.enderdragon.breathweapon.adultbreathefirestart"),
  ADULT_BREATHE_FIRE_LOOP("mob.enderdragon.breathweapon.adultbreathefireloop"),
  ADULT_BREATHE_FIRE_STOP("mob.enderdragon.breathweapon.adultbreathefirestop"),
  JUVENILE_BREATHE_FIRE_START("mob.enderdragon.breathweapon.juvenilebreathefirestart"),
  JUVENILE_BREATHE_FIRE_LOOP("mob.enderdragon.breathweapon.juvenilebreathefireloop"),
  JUVENILE_BREATHE_FIRE_STOP("mob.enderdragon.breathweapon.juvenilebreathefirestop"),
  HATCHLING_BREATHE_FIRE_START("mob.enderdragon.breathweapon.hatchlingbreathefirestart"),
  HATCHLING_BREATHE_FIRE_LOOP("mob.enderdragon.breathweapon.hatchlingbreathefireloop"),
  HATCHLING_BREATHE_FIRE_STOP("mob.enderdragon.breathweapon.hatchlingbreathefirestop");

  public final String getJsonName() {return DragonMounts.AID + ":" + jsonName;}

  private SoundEffectNames(String i_jsonName) {
    jsonName = i_jsonName;
  }
  private final String jsonName;
}
