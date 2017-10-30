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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Biomes;
import net.minecraft.util.DamageSource;

import java.util.UUID;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedAir extends DragonBreed {

	public static final UUID MODIFIER_ID = UUID.fromString("60be8770-29f2-4bbe-bb8c-7a41143c9974");
	public static final AttributeModifier MODIFIER = new AttributeModifier(MODIFIER_ID, "Air dragon speed bonus", 0.2, 2).setSaved(false);

	DragonBreedAir() {
		super("aether", 0x1dc4f3);

		addImmunity(DamageSource.MAGIC);

		addHabitatBiome(Biomes.EXTREME_HILLS);
	}

	@Override
	public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
		// true if located pretty high (> 2/3 of the maximum world height)
		return dragon.posY > dragon.world.getHeight() * 0.66;
	}

	@Override
	public void onEnable(EntityTameableDragon dragon) {
		dragon.getAttributeMap().getAttributeInstance(EntityTameableDragon.MOVEMENT_SPEED_AIR).applyModifier(MODIFIER);
	}

	@Override
	public void onDisable(EntityTameableDragon dragon) {
		dragon.getAttributeMap().getAttributeInstance(EntityTameableDragon.MOVEMENT_SPEED_AIR).removeModifier(MODIFIER);
	}

	@Override
	public void onDeath(EntityTameableDragon dragon) {
	}
}
