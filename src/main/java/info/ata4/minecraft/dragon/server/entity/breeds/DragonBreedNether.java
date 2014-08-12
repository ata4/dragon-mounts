/*
 ** 2013 November 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.BiomeGenBase;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedNether extends DragonBreed {

    public DragonBreedNether() {
        super("nether", "nether", 0x793838);
        
        addImmunity(DamageSource.inFire);
        addImmunity(DamageSource.onFire);
        addImmunity(DamageSource.lava);
        
        addHabitatBlock(Blocks.netherrack);
        addHabitatBlock(Blocks.soul_sand);
        addHabitatBlock(Blocks.nether_brick);
        addHabitatBlock(Blocks.nether_brick_fence);
        addHabitatBlock(Blocks.nether_brick_stairs);
        addHabitatBlock(Blocks.nether_wart);
        addHabitatBlock(Blocks.glowstone);
        addHabitatBlock(Blocks.quartz_ore);
        
        addHabitatBiome(BiomeGenBase.hell);
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
        dragon.getNavigator().setAvoidsWater(true);
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
        dragon.getNavigator().setAvoidsWater(false);
    }
}
