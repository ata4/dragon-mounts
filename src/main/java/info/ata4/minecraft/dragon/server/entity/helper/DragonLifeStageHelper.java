/*
 ** 2013 October 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonLifeStageHelper extends DragonHelper {
    
    private static final Logger L = LogManager.getLogger();
    
    private DragonLifeStage lifeStagePrev;
    private DragonSizeModifier sizeModifier = new DragonSizeModifier();
    private int eggWiggleX;
    private int eggWiggleZ;

    public DragonLifeStageHelper(EntityTameableDragon dragon) {
        super(dragon);
    }
    
    @Override
    public void applyEntityAttributes() {
        sizeModifier.setSize(getSize());

        dragon.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(sizeModifier);
        dragon.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(sizeModifier);
    }
    
    /**
     * Generates some egg shell particles and a breaking sound.
     */
    public void playEggCrackEffect() {
        int bx = (int) Math.round(dragon.posX - 0.5);
        int by = (int) Math.round(dragon.posY);
        int bz = (int) Math.round(dragon.posZ - 0.5);
        dragon.worldObj.playAuxSFX(2001, bx, by, bz, Block.getIdFromBlock(Blocks.dragon_egg));
    }
    
    public int getEggWiggleX() {
        return eggWiggleX;
    }

    public int getEggWiggleZ() {
        return eggWiggleZ;
    }
    
    /**
     * Returns the size multiplier for the current age.
     * 
     * @return size
     */
    public float getSize() {
        // constant size for egg stage
        if (getLifeStage().isEgg()) {
            return 0.2f;
        }
        
        int age = dragon.getGrowingAge();
        int ageEgg = DragonLifeStage.EGG.getAgeLimit();
        
        return 1 - (age / (float) ageEgg);
    }
    
    /**
     * Transforms the dragon to an egg (item form)
     */
    public void transformToEgg() {
        if (dragon.getHealth() <= 0) {
            // no can do
            return;
        }
        
        L.debug("transforming to egg");

        float volume = 1;
        float pitch = 0.5f + (0.5f - rand.nextFloat()) * 0.1f;
        dragon.worldObj.playSoundAtEntity(dragon, "mob.endermen.portal", volume, pitch);
        
        if (dragon.isSaddled()) {
            dragon.dropItem(Items.saddle, 1);
        }
        
        dragon.entityDropItem(new ItemStack(Blocks.dragon_egg), 0);
        dragon.setDead();
    }
    
    /**
     * Returns the current life stage of the dragon.
     * 
     * @return current life stage
     */
    public DragonLifeStage getLifeStage() {
        return DragonLifeStage.valueOf(dragon.getGrowingAge());
    }
    
    /**
     * Sets a new life stage for the dragon.
     * 
     * @param lifeStage new life stage
     */
    public final void setLifeStage(DragonLifeStage lifeStage) {
        L.trace("setLifeStage({})", lifeStage);
        // onNewLifeStage will be triggered next tick
        dragon.setGrowingAge(lifeStage.getAgeLimit());
    }
    
    /**
     * Called when the dragon enters a new life stage.
     */ 
    private void onNewLifeStage(DragonLifeStage lifeStage, DragonLifeStage prevLifeStage) {
        L.trace("onNewLifeStage({},{})", prevLifeStage, lifeStage);
        // update collision box size
        if (lifeStage.isEgg()) {
            dragon.updateSize(0.98f);
        } else {
            dragon.updateSize(EntityTameableDragon.BASE_SIZE * getSize());
        }
        
        if (dragon.isClient()) {
            if (prevLifeStage != null && prevLifeStage.isEgg() && lifeStage.isHatchling()) {
                playEggCrackEffect();
            }
        } else {
            // eggs and hatchlings can't fly
            dragon.setCanFly(!lifeStage.isEgg() && !lifeStage.isHatchling());
            
            // only hatchlings are small enough for doors
            // (eggs don't move on their own anyway and are ignored)
            dragon.getNavigator().setEnterDoors(lifeStage.isHatchling());
            
            // update AI states so the egg won't move
            if (lifeStage.isEgg()) {
                dragon.setPathToEntity(null);
                dragon.setAttackTarget(null);
            }
            
            // update attribute modifier
            IAttributeInstance healthAttrib = dragon.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            IAttributeInstance damageAttrib = dragon.getEntityAttribute(SharedMonsterAttributes.attackDamage);

            // remove old size modifiers
            healthAttrib.removeModifier(sizeModifier);
            damageAttrib.removeModifier(sizeModifier);

            // update modifier
            sizeModifier.setSize(getSize());

            // set new size modifiers
            healthAttrib.applyModifier(sizeModifier);
            damageAttrib.applyModifier(sizeModifier);

            // heal dragon to updated full health
            dragon.setHealth(dragon.getMaxHealth());
        }
    }

    @Override
    public void onLivingUpdate() {
        // trigger event when a new life stage was reached
        DragonLifeStage lifeStage = getLifeStage();
        if (lifeStagePrev != lifeStage) {
            onNewLifeStage(lifeStage, lifeStagePrev);
            lifeStagePrev = lifeStage;
        }
        
        if (lifeStage.isEgg()) {
            int age = dragon.getGrowingAge();
            
            // animate egg wiggle based on the time the eggs take to hatch
            int eggAge = DragonLifeStage.EGG.getAgeLimit();
            int hatchAge = DragonLifeStage.HATCHLING.getAgeLimit();
            float chance = (age - eggAge) / (float) (hatchAge - eggAge);

            // wait until the egg is nearly hatched
            if (chance > 0.66f) {
                // reduce chance so it can run every tick
                chance /= 60;

                if (eggWiggleX > 0) {
                    eggWiggleX--;
                } else if (rand.nextFloat() < chance) {
                    eggWiggleX = rand.nextBoolean() ? 10 : 20;
                    playEggCrackEffect();
                }

                if (eggWiggleZ > 0) {
                    eggWiggleZ--;
                } else if (rand.nextFloat() < chance) {
                    eggWiggleZ = rand.nextBoolean() ? 10 : 20;
                    playEggCrackEffect();
                }
            }

            // spawn generic particles
            double px = dragon.posX + (rand.nextDouble() - 0.5);
            double py = dragon.posY + (rand.nextDouble() - 0.5);
            double pz = dragon.posZ + (rand.nextDouble() - 0.5);
            double ox = (rand.nextDouble() - 0.5) * 2;
            double oy = (rand.nextDouble() - 0.5) * 2;
            double oz = (rand.nextDouble() - 0.5) * 2;
            dragon.worldObj.spawnParticle("portal", px, py, pz, ox, oy, oz);
        }
    }

    @Override
    public void onDeath() {
        if (dragon.isClient() && getLifeStage().isEgg()) {
            playEggCrackEffect();
        }
    }
}
