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

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.MathHelper;

/**
 * Enum for dragon life stages. Used as aliases for the age value of dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum DragonLifeStage {
    
    EGG(0, 24000, 0.25f),
    HATCHLING(24000, 24000, 0.33f),
    JUVENILE(48000, 24000, 0.66f),
    ADULT(72000, -1, 1);
    
    public final int startTicks;
    public final int durationTicks; // -1 means infinite
    public final float scale;
    
    DragonLifeStage(int startTicks, int durationTicks, float scale) {
        this.startTicks = startTicks;
        this.durationTicks = durationTicks;
        this.scale = scale;
    }

    public static DragonLifeStage getLifeStageFromTickCount(int ticksSinceCreation) {
        if (ticksSinceCreation < HATCHLING.startTicks) {
            return EGG;
        }
        if (ticksSinceCreation < JUVENILE.startTicks) {
            return HATCHLING;
        }
        if (ticksSinceCreation < ADULT.startTicks) {
            return JUVENILE;
        }
        return ADULT;
    }
    
    public static float getScaleFromTickCount(int ticksSinceCreation) {
        DragonLifeStage lifeStage = getLifeStageFromTickCount(ticksSinceCreation);
        int timeInThisStage = ticksSinceCreation - lifeStage.startTicks;
        float fractionOfStage = timeInThisStage / (float) lifeStage.durationTicks;
        
        switch (lifeStage) {
            // constant size for egg and adult stage
            case EGG:
                return EGG.scale;
                
            case ADULT:
                return ADULT.scale;
            
            // interpolated size for hatchling and juvenile stages
            case HATCHLING:
                return MathX.lerp(HATCHLING.scale, JUVENILE.scale, fractionOfStage);
                
            case JUVENILE:
                return MathX.lerp(JUVENILE.scale, ADULT.scale, fractionOfStage);
            
            // this should never happen unless more life stages have been added
            // without updating this method
            default:
                throw new RuntimeException("Unimplemented life stage: " + lifeStage);
        }
    }

    public static int clipTickCountToValid(int ticksSinceCreation) {
        return MathHelper.clamp_int(
                ticksSinceCreation,
                EGG.startTicks,
                ADULT.startTicks
        );
    }
}
