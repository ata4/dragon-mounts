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
    
    EGG(-72000),
    HATCHLING(-48000),
    JUVENILE(-24000),
    ADULT(0);
    
    public static DragonLifeStage valueOf(int age) {
        if (age >= ADULT.ageLimit) {
            return ADULT;
        } else if (age >= JUVENILE.ageLimit) {
            return JUVENILE;
        } else if (age >= HATCHLING.ageLimit) {
            return HATCHLING;
        } else {
            return EGG;
        }
    }
    
    private final int ageLimit;

    private DragonLifeStage(int ageLimit) {
        this.ageLimit = ageLimit;
    }

    /**
     * @return the age limit in ticks
     */
    public int getAgeLimit() {
        return ageLimit;
    }

    public boolean isEgg() {
        return this == EGG;
    }
    
    public boolean isHatchling() {
        return this == HATCHLING;
    }
    
    public boolean isJuvenile() {
        return this == JUVENILE;
    }
    
    public boolean isAdult() {
        return this == ADULT;
    }
}
