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
    
    public final int ageLimit;

    private DragonLifeStage(int ageLimit) {
        this.ageLimit = ageLimit;
    }

}
