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
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Base class for dragon breeds.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreed {
    
    private final String name;
    private final String skin;
    private final int color;
    private Set<String> immunities = new HashSet<String>();
    private Set<Block> breedBlocks = new HashSet<Block>();
    private Set<BiomeGenBase> biomes = new HashSet<BiomeGenBase>();
    
    public DragonBreed(String name, String skin, int color) {
        this.name = name;
        this.skin = skin;
        this.color = color;
        
        // ignore suffocation damage
        addImmunity(DamageSource.drown);
        addImmunity(DamageSource.inWall);
        
        // assume that cactus needles don't do much damage to animals with horned scales
        addImmunity(DamageSource.cactus);
    }
    
    public String getName() {
        return name;
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
    
    public void onEnable(EntityTameableDragon dragon) {
    }
    
    public void onDisable(EntityTameableDragon dragon) {
    }
    
    public void onUpdate(EntityTameableDragon dragon) {
    }
    
    public void onDeath(EntityTameableDragon dragon) {
    }
    
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
        return DragonMounts.AID + ":mob.enderdragon.death";
    }
    
    @Override
    public String toString() {
        return name;
    }
}
