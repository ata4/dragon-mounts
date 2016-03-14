/*
** 2016 März 14
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonSoundManager extends DragonHelper {
    
    public DragonSoundManager(EntityTameableDragon dragon) {
        super(dragon);
    }
    
    /**
     * Returns the sound this mob makes while it's alive.
     */
    public String getLivingSound() {
        if (dragon.isEgg() || dragon.isFlying()) {
            return null;
        } else {
            return dragon.getBreed().getLivingSound(dragon);
        }
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    public String getHurtSound() {
        if (dragon.isEgg()) {
            return "mob.zombie.wood";
        } else {
            return dragon.getBreed().getHurtSound(dragon);
        }
    }
    
    /**
     * Returns the sound this mob makes on death.
     */
    public String getDeathSound() {
        if (dragon.isEgg()) {
            return "mob.zombie.woodbreak";
        } else {
            return dragon.getBreed().getDeathSound(dragon);
        }
    }
    
    public String getWingsSound() {
        return "mob.enderdragon.wings";
    }
    
    public String getStepSound() {
        return DragonMounts.AID + ":mob.enderdragon.step";
    }
    
    public String getEatSound() {
        return "random.eat";
    }
    
    public String getAttackSound() {
        return "random.eat";
    }
    
    /**
     * Plays living's sound at its position
     */
    public void playLivingSound() {
        String sound = getLivingSound();
        if (sound == null) {
            return;
        }
        
        playSound(sound, 1, 1);
    }
    
    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getTalkInterval() {
        return 240;
    }
    
    /**
     * Client side method for wing animations. Plays wing flapping sounds.
     * 
     * @param speed wing animation playback speed
     */
    public void onWingsDown(float speed) {
        if (!dragon.isInWater()) {
            // play wing sounds
            float pitch = (1 - speed);
            float volume = 0.3f + (1 - speed) * 0.2f;
            playSound(getWingsSound(), volume, pitch, true);
        }
    }
    
    /**
     * Plays step sound at given x, y, z for the entity
     */
    public void playStepSound(BlockPos entityPos, Block block) {
        // no sounds for eggs or underwater action
        if (dragon.isEgg() || dragon.isInWater()) {
            return;
        }
        
        // override sound type if the top block is snowy
        Block.SoundType soundType;
        if (dragon.worldObj.getBlockState(entityPos.up()).getBlock() == Blocks.snow_layer) {
            soundType = Blocks.snow_layer.stepSound;
        } else {
            soundType = block.stepSound;
        }
        
        // play stomping for bigger dragons
        String stepSound;
        if (dragon.isHatchling()) {
            stepSound = soundType.getStepSound();
        } else {
            stepSound = getStepSound();
        }
        
        playSound(stepSound, soundType.getVolume(), soundType.getFrequency());
    }
    
    public void playSound(String name, float volume, float pitch, boolean local) {
        if (name == null || dragon.isSilent()) {
            return;
        }
        
        volume *= getVolume(name);
        pitch *= getPitch(name);

        if (local) {
            dragon.worldObj.playSound(dragon.posX, dragon.posY, dragon.posZ,
                    name, volume, pitch, false);
        } else {
            dragon.worldObj.playSoundAtEntity(dragon, name, volume, pitch);
        }
    }
    
    public void playSound(String name, float volume, float pitch) {
        playSound(name, volume, pitch, false);
    }
    
    /**
     * Returns the volume for a sound to play.
     */
    public float getVolume(String name) {
        return dragon.getScale() * dragon.getBreed().getSoundVolume(dragon, name);
    }
    
    /**
     * Returns the pitch for a sound to play.
     */
    public float getPitch(String name) {
        return (2.0f - dragon.getScale()) * dragon.getBreed().getSoundPitch(dragon, name);
    }
}
