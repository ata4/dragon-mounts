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

import net.minecraft.util.MathHelper;

/**
 * Enum for dragon life stages. Used as aliases for the age value of dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum DragonLifeStage {
    
    EGG(0, 24000),
    HATCHLING(24000, 24000),
    JUVENILE(48000, 24000),
    ADULT(72000, -1);
    
    DragonLifeStage(int startOfStageInTicks, int stageDurationTicks) {
        this.stageDurationTicks = stageDurationTicks;
        this.startOfStageInTicks = startOfStageInTicks;
    }

    public static DragonLifeStage getLifeStageFromTickCount(int ticksSinceCreation) {
        if (ticksSinceCreation < HATCHLING.startOfStageInTicks) {
            return EGG;
        }
        if (ticksSinceCreation < JUVENILE.startOfStageInTicks) {
            return HATCHLING;
        }
        if (ticksSinceCreation < ADULT.startOfStageInTicks) {
            return JUVENILE;
        }
        return ADULT;
    }

    public static int clipTickCountToValid(int ticksSinceCreation) {
        return MathHelper.clamp_int(
                ticksSinceCreation,
                EGG.getStartOfStageInTicks(),
                ADULT.getStartOfStageInTicks()
        );
    }

    // -1 means infinite
    public int getDurationInTicks() {
        return stageDurationTicks;
    }

    public int getStartOfStageInTicks() {
        return startOfStageInTicks;
    }

    private final int stageDurationTicks;
    private final int startOfStageInTicks;
}
