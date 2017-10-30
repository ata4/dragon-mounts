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
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedNether extends DragonBreed {

	DragonBreedNether() {
		super("nether", 0x793838);

		addImmunity(DamageSource.IN_FIRE);
		addImmunity(DamageSource.ON_FIRE);
		addImmunity(DamageSource.LAVA);

		addHabitatBlock(Blocks.NETHERRACK);
		addHabitatBlock(Blocks.SOUL_SAND);
		addHabitatBlock(Blocks.NETHER_BRICK);
		addHabitatBlock(Blocks.NETHER_BRICK_FENCE);
		addHabitatBlock(Blocks.NETHER_BRICK_STAIRS);
		addHabitatBlock(Blocks.NETHER_WART);
		addHabitatBlock(Blocks.GLOWSTONE);
		addHabitatBlock(Blocks.QUARTZ_ORE);

		addHabitatBiome(Biomes.HELL);
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
