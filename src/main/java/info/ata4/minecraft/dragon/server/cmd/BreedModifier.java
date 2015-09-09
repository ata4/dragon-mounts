/*
 ** 2015 Juni 30
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.cmd;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.DragonBreed;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class BreedModifier implements EntityModifier {
    
    private final DragonBreed breed;

    BreedModifier(DragonBreed breed) {
        this.breed = breed;
    }

    @Override
    public void modify(EntityTameableDragon dragon) {
        dragon.setBreed(breed);
    }
}
