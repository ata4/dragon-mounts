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

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

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
    public SoundEvent getLivingSound() {
        if (dragon.isEgg() || dragon.isFlying()) {
            return null;
        } else {
            return dragon.getBreed().getLivingSound();
        }
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    public SoundEvent getHurtSound() {
        if (dragon.isEgg()) {
            return SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD;
        } else {
            return dragon.getBreed().getHurtSound();
        }
    }
    
    /**
     * Returns the sound this mob makes on death.
     */
    public SoundEvent getDeathSound() {
        if (dragon.isEgg()) {
            return SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD;
        } else {
            return dragon.getBreed().getDeathSound();
        }
    }
    
    public SoundEvent getWingsSound() {
        return dragon.getBreed().getWingsSound();
    }
    
    public SoundEvent getStepSound() {
        return dragon.getBreed().getStepSound();
    }
    
    public SoundEvent getEatSound() {
        return dragon.getBreed().getEatSound();
    }
    
    public SoundEvent getAttackSound() {
        return dragon.getBreed().getAttackSound();
    }
    
    /**
     * Plays living's sound at its position
     */
    public void playLivingSound() {
        SoundEvent sound = getLivingSound();
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
        SoundType soundType;
        if (dragon.worldObj.getBlockState(entityPos.up()).getBlock() == Blocks.SNOW_LAYER) {
            soundType = Blocks.SNOW_LAYER.getSoundType();
        } else {
            soundType = block.getSoundType();
        }
        
        // play stomping for bigger dragons
        SoundEvent stepSound;
        if (dragon.isHatchling()) {
            stepSound = soundType.getStepSound();
        } else {
            stepSound = getStepSound();
        }
        
        playSound(stepSound, soundType.getVolume(), soundType.getPitch());
    }
    
    public void playSound(SoundEvent sound, float volume, float pitch, boolean local) {
        if (sound == null || dragon.isSilent()) {
            return;
        }
        
        volume *= getVolume(sound);
        pitch *= getPitch(sound);

        if (local) {
            dragon.worldObj.playSound(dragon.posX, dragon.posY, dragon.posZ,
                    sound, dragon.getSoundCategory(), volume, pitch, false);
        } else {
            dragon.worldObj.playSound(null, dragon.posX, dragon.posY, dragon.posZ,
                    sound, dragon.getSoundCategory(), volume, pitch);
        }
    }
    
    public void playSound(SoundEvent sound, float volume, float pitch) {
        playSound(sound, volume, pitch, false);
    }
    
    /**
     * Returns the volume for a sound to play.
     */
    public float getVolume(SoundEvent sound) {
        return dragon.getScale() * dragon.getBreed().getSoundVolume(sound);
    }
    
    /**
     * Returns the pitch for a sound to play.
     */
    public float getPitch(SoundEvent sound) {
        return (2.0f - dragon.getScale()) * dragon.getBreed().getSoundPitch(sound);
    }
}
