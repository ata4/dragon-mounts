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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Base class for dragon breeds.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class DragonBreed {
    
    private final EnumDragonBreed type;
    private final String skin;
    private final int color;
    private final Set<String> immunities = new HashSet<>();
    private final Set<Block> breedBlocks = new HashSet<>();
    private final Set<BiomeGenBase> biomes = new HashSet<>();
    protected final Random rand = new Random();
    
    DragonBreed(EnumDragonBreed type, String skin, int color) {
        this.type = type;
        this.skin = skin;
        this.color = color;
        
        // ignore suffocation damage
        addImmunity(DamageSource.drown);
        addImmunity(DamageSource.inWall);
        
        // assume that cactus needles don't do much damage to animals with horned scales
        addImmunity(DamageSource.cactus);
    }
    
    public String getName() {
        return type.getName();
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
    
    protected final void addHabitatBiome(BiomeGenBase biome) {
        biomes.add(biome);
    }
    
    public boolean isHabitatBiome(BiomeGenBase biome) {
        return biomes.contains(biome);
    }
    
    public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
        return false;
    }
    
    public Item[] getFoodItems() {
        return new Item[] { Items.porkchop, Items.beef, Items.chicken };
    }
    
    public Item getBreedingItem() {
        return Items.fish;
    }
    
    public abstract void onEnable(EntityTameableDragon dragon);
    
    public abstract void onDisable(EntityTameableDragon dragon);
    
    public abstract void onUpdate(EntityTameableDragon dragon);
    
    public abstract void onDeath(EntityTameableDragon dragon);
    
    public SoundEvent getLivingSound() {
        if (rand.nextInt(3) == 0) {
            return SoundEvents.entity_enderdragon_growl;
        } else {
            return DragonMountsSoundEvents.entity_dragon_mount_breathe;
        }
    }
    
    public SoundEvent getHurtSound() {
        return SoundEvents.entity_enderdragon_hurt;
    }
    
    public SoundEvent getDeathSound() {
        return DragonMountsSoundEvents.entity_dragon_mount_death;
    }
    
    public SoundEvent getWingsSound() {
        return SoundEvents.entity_enderdragon_flap;
    }
    
    public SoundEvent getStepSound() {
        return DragonMountsSoundEvents.entity_dragon_mount_step;
    }
    
    public SoundEvent getEatSound() {
        return SoundEvents.entity_generic_eat;
    }
    
    public SoundEvent getAttackSound() {
        return SoundEvents.entity_generic_eat;
    }

    public float getSoundPitch(SoundEvent sound) {
        return 1;
    }

    public float getSoundVolume(SoundEvent sound) {
        return 1;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
