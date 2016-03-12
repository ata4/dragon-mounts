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

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for dragon breeds.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class DragonBreed {
    
    private final EnumDragonBreed type;
    private final String skin;
    private final int color;
    private final Set<String> immunities = new HashSet<String>();
    private final Set<Block> breedBlocks = new HashSet<Block>();
    private final Set<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
    
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
    
    protected void addImmunity(DamageSource dmg) {
        immunities.add(dmg.damageType);
    }
    
    public boolean isImmuneToDamage(DamageSource dmg) {
        if (immunities.isEmpty()) {
            return false;
        }
        
        return immunities.contains(dmg.damageType);
    }
    
    public void addHabitatBlock(Block block) {
        breedBlocks.add(block);
    }
    
    public boolean isHabitatBlock(Block block) {
        return breedBlocks.contains(block);
    }
    
    public void addHabitatBiome(BiomeGenBase biome) {
        biomes.add(biome);
    }
    
    public boolean isHabitatBiome(BiomeGenBase biome) {
        return biomes.contains(biome);
    }
    
    public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
        return false;
    }
    
    public abstract void onEnable(EntityTameableDragon dragon);
    
    public abstract void onDisable(EntityTameableDragon dragon);
    
    public abstract void onUpdate(EntityTameableDragon dragon);
    
    public abstract void onDeath(EntityTameableDragon dragon);
    
    public String getLivingSound(EntityTameableDragon dragon) {
        if (dragon.getRNG().nextInt(3) == 0) {
            return "mob.enderdragon.growl";
        } else {
            return DragonMounts.AID + ":mob.enderdragon.breathe";
        }
    }
    
    public String getHurtSound(EntityTameableDragon dragon) {
        return "mob.enderdragon.hit";
    }
    
    public String getDeathSound(EntityTameableDragon dragon) {
//        return DragonMounts.AID + ":mob.enderdragon.death";
        return "";
    }
    
    public float getSoundPitch(EntityTameableDragon dragon, String sound) {
        // lower pitch for default breathing sounds
        if (sound.endsWith("mob.enderdragon.breathe")) {
            return 0.5f;
        }
        
        return 1;
    }
    
    public float getSoundVolume(EntityTameableDragon dragon, String sound) {
        // lower volume for default breathing sounds
        if (sound.endsWith("mob.enderdragon.breathe")) {
            return 0.5f;
        }
        
        return 1.0f;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
