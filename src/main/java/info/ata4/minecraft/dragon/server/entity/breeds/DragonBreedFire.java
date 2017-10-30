/*
 ** 2013 October 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedFire extends DragonBreed {

	DragonBreedFire() {
		super("fire", 0x960b0f);

		addImmunity(DamageSource.IN_FIRE);
		addImmunity(DamageSource.ON_FIRE);
		addImmunity(DamageSource.LAVA);

		addHabitatBlock(Blocks.LAVA);
		addHabitatBlock(Blocks.FLOWING_LAVA);
		addHabitatBlock(Blocks.FIRE);
		addHabitatBlock(Blocks.LIT_FURNACE);

		addHabitatBiome(Biomes.DESERT);
		addHabitatBiome(Biomes.DESERT_HILLS);
	}

	@Override
	public void onEnable(EntityTameableDragon dragon) {
		dragon.getBrain().setAvoidsWater(true);
	}

	@Override
	public void onDisable(EntityTameableDragon dragon) {
		dragon.getBrain().setAvoidsWater(false);
	}

	@Override
	public void onDeath(EntityTameableDragon dragon) {
	}
}
