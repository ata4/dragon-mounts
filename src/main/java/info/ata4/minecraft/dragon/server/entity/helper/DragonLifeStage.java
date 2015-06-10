/*
 ** 2012 August 23
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

/**
 * Enum for dragon life stages. Used as aliases for the age value of dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum DragonLifeStage {
    
    EGG(0, 24000),
    HATCHLING(24000, 24000),
    JUVENILE(48000, 24000),
    ADULT(72000, 0);
    
    DragonLifeStage(int i_startOfStageInTicks, int i_stageDurationTicks) {
    this.stageDurationTicks = i_stageDurationTicks;
    this.startOfStageInTicks = i_startOfStageInTicks;
    }

    public static DragonLifeStage getLifeStageFromTickCount(int ticksSinceCreation)
    {
      if (ticksSinceCreation < HATCHLING.startOfStageInTicks) return EGG;
      if (ticksSinceCreation < JUVENILE.startOfStageInTicks) return HATCHLING;
      if (ticksSinceCreation < ADULT.startOfStageInTicks) return JUVENILE;
      return ADULT;
    }

    public int getDurationInTicks() {return stageDurationTicks;}

    private final int stageDurationTicks;
    private final int startOfStageInTicks;
}
