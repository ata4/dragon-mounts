/*
 ** 2013 March 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import info.ata4.minecraft.dragon.DragonMountsSoundEvents;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Base class for dragon breeds.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class DragonBreed {

	protected final Random rand = new Random();
	private final String skin;
	private final int color;
	private final Set<String> immunities = new HashSet<>();
	private final Set<Block> breedBlocks = new HashSet<>();
	private final Set<Biome> biomes = new HashSet<>();

	DragonBreed(String skin, int color) {
		this.skin = skin;
		this.color = color;

		// ignore suffocation damage
		addImmunity(DamageSource.DROWN);
		addImmunity(DamageSource.IN_WALL);

		// assume that cactus needles don't do much damage to animals with horned scales
		addImmunity(DamageSource.CACTUS);

		// ignore damage from vanilla ender dragon
		addImmunity(DamageSource.DRAGON_BREATH);
	}

	public String getSkin() {
		return skin;
	}

	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEFINED;
	}

	public int getColor() {
		return color;
	}

	public float getColorR() {
		return ((color >> 16) & 0xFF) / 255f;
	}

	public float getColorG() {
		return ((color >> 8) & 0xFF) / 255f;
	}

	public float getColorB() {
		return (color & 0xFF) / 255f;
	}

	protected final void addImmunity(DamageSource dmg) {
		immunities.add(dmg.damageType);
	}

	public boolean isImmuneToDamage(DamageSource dmg) {
		if (immunities.isEmpty()) {
			return false;
		}

		return immunities.contains(dmg.damageType);
	}

	protected final void addHabitatBlock(Block block) {
		breedBlocks.add(block);
	}

	public boolean isHabitatBlock(Block block) {
		return breedBlocks.contains(block);
	}

	protected final void addHabitatBiome(Biome biome) {
		biomes.add(biome);
	}

	public boolean isHabitatBiome(Biome biome) {
		return biomes.contains(biome);
	}

	public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
		return false;
	}

	public Item[] getFoodItems() {
		return new Item[]{Items.PORKCHOP, Items.BEEF, Items.CHICKEN};
	}

	public Item getBreedingItem() {
		return Items.FISH;
	}

	public void onUpdate(EntityTameableDragon dragon) {
		placeFootprintBlocks(dragon);
	}

	protected void placeFootprintBlocks(EntityTameableDragon dragon) {
		// only apply on server
		if (!dragon.isServer()) {
			return;
		}

		// only apply on adult dragons that don't fly
		if (!dragon.isAdult() || dragon.isFlying()) {
			return;
		}

		// only apply if footprints are enabled
		float footprintChance = getFootprintChance();
		if (footprintChance == 0) {
			return;
		}

		// footprint loop, from EntitySnowman.onLivingUpdate with slight tweaks
		World world = dragon.world;
		for (int i = 0; i < 4; i++) {
			// place only if randomly selected
			if (world.rand.nextFloat() > footprintChance) {
				continue;
			}

			// get footprint position
			double bx = dragon.posX + (i % 2 * 2 - 1) * 0.25;
			double by = dragon.posY + 0.5;
			double bz = dragon.posZ + (i / 2 % 2 * 2 - 1) * 0.25;
			BlockPos pos = new BlockPos(bx, by, bz);

			// footprints can only be placed on empty space
			if (world.isAirBlock(pos)) {
				continue;
			}

			placeFootprintBlock(dragon, pos);
		}
	}

	protected void placeFootprintBlock(EntityTameableDragon dragon, BlockPos blockPos) {
	}

	protected float getFootprintChance() {
		return 0;
	}

	public abstract void onEnable(EntityTameableDragon dragon);

	public abstract void onDisable(EntityTameableDragon dragon);

	public abstract void onDeath(EntityTameableDragon dragon);

	public SoundEvent getLivingSound() {
		if (rand.nextInt(3) == 0) {
			return SoundEvents.ENTITY_ENDERDRAGON_GROWL;
		} else {
			return DragonMountsSoundEvents.ENTITY_DRAGON_MOUNT_BREATHE;
		}
	}

	public SoundEvent getHurtSound() {
		return SoundEvents.ENTITY_ENDERDRAGON_HURT;
	}

	public SoundEvent getDeathSound() {
		return DragonMountsSoundEvents.ENTITY_DRAGON_MOUNT_DEATH;
	}

	public SoundEvent getWingsSound() {
		return SoundEvents.ENTITY_ENDERDRAGON_FLAP;
	}

	public SoundEvent getStepSound() {
		return DragonMountsSoundEvents.ENTITY_DRAGON_MOUNT_STEP;
	}

	public SoundEvent getEatSound() {
		return SoundEvents.ENTITY_GENERIC_EAT;
	}

	public SoundEvent getAttackSound() {
		return SoundEvents.ENTITY_GENERIC_EAT;
	}

	public float getSoundPitch(SoundEvent sound) {
		return 1;
	}

	public float getSoundVolume(SoundEvent sound) {
		return 1;
	}
}
