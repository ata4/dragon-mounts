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

import info.ata4.minecraft.dragon.server.block.BlockDragonBreedEgg;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import static info.ata4.minecraft.dragon.server.entity.helper.EnumDragonLifeStage.*;
import info.ata4.minecraft.dragon.server.util.ClientServerSynchronisedTickCount;
import net.minecraft.block.Block;
import static net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE;
import static net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.EnumParticleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonLifeStageHelper extends DragonHelper {
    
    private static final Logger L = LogManager.getLogger();
    
    private static final String NBT_TICKS_SINCE_CREATION = "TicksSinceCreation";
    private static final int TICKS_SINCE_CREATION_UPDATE_INTERVAL = 100;
    private static final float EGG_CRACK_THRESHOLD = 0.9f;
    private static final float EGG_WIGGLE_THRESHOLD = 0.75f;
    private static final float EGG_WIGGLE_BASE_CHANCE = 20;
    
    private EnumDragonLifeStage lifeStagePrev;
    private int eggWiggleX;
    private int eggWiggleZ;
    
    // the ticks since creation is used to control the dragon's life stage.  It is only updated by the server occasionally.
    // the client keeps a cached copy of it and uses client ticks to interpolate in the gaps.
    // when the watcher is updated from the server, the client will tick it faster or slower to resynchronise
    private final DataParameter<Integer> dataParam;
    private int ticksSinceCreationServer;
    private final ClientServerSynchronisedTickCount ticksSinceCreationClient;

    public DragonLifeStageHelper(EntityTameableDragon dragon, DataParameter<Integer> dataParam) {
        super(dragon);
        
        this.dataParam = dataParam;
        dataWatcher.register(dataParam, ticksSinceCreationServer);
        
        if (dragon.isClient()) {
            ticksSinceCreationClient = new ClientServerSynchronisedTickCount(TICKS_SINCE_CREATION_UPDATE_INTERVAL);
            ticksSinceCreationClient.reset(ticksSinceCreationServer);
        } else {
            ticksSinceCreationClient = null;
        }
    }
    
    @Override
    public void applyEntityAttributes() {
        applyScaleModifier(MAX_HEALTH);
        applyScaleModifier(ATTACK_DAMAGE);
    }
    
    private void applyScaleModifier(IAttribute attribute) {
        IAttributeInstance instance = dragon.getEntityAttribute(attribute);
        AttributeModifier oldModifier = instance.getModifier(DragonScaleModifier.ID);
        if (oldModifier != null) {
            instance.removeModifier(oldModifier);
        }
        instance.applyModifier(new DragonScaleModifier(getScale()));
    }
    
    /**
     * Generates some egg shell particles and a breaking sound.
     */
    public void playEggCrackEffect() {
        dragon.worldObj.playEvent(2001, dragon.getPosition(),
                Block.getIdFromBlock(BlockDragonBreedEgg.INSTANCE));
    }
    
    public int getEggWiggleX() {
        return eggWiggleX;
    }

    public int getEggWiggleZ() {
        return eggWiggleZ;
    }
    
    /**
     * Returns the current life stage of the dragon.
     *
     * @return current life stage
     */
    public EnumDragonLifeStage getLifeStage() {
        int age = getTicksSinceCreation();
        return EnumDragonLifeStage.fromTickCount(age);
    }

    public int getTicksSinceCreation() {
        if (dragon.isServer()) {
            return ticksSinceCreationServer;
        } else {
            return ticksSinceCreationClient.getCurrentTickCount();
        }
    }
    
    public void setTicksSinceCreation(int ticksSinceCreation) {
        if (dragon.isServer()) {
            ticksSinceCreationServer = ticksSinceCreation;
        } else {
            ticksSinceCreationClient.updateFromServer(ticksSinceCreationServer);
        }
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger(NBT_TICKS_SINCE_CREATION, getTicksSinceCreation());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        int ticksRead = nbt.getInteger(NBT_TICKS_SINCE_CREATION);
        ticksRead = EnumDragonLifeStage.clampTickCount(ticksRead);
        ticksSinceCreationServer = ticksRead;
        dataWatcher.set(dataParam, ticksSinceCreationServer);
    }
    
    /**
     * Returns the size multiplier for the current age.
     * 
     * @return size
     */
    public float getScale() {
        return EnumDragonLifeStage.scaleFromTickCount(getTicksSinceCreation());
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
        dragon.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, volume, pitch);
        
        if (dragon.isSaddled()) {
            dragon.dropItem(Items.SADDLE, 1);
        }
        
        dragon.entityDropItem(new ItemStack(BlockDragonBreedEgg.INSTANCE),
            dragon.getBreedType().getMeta());
        
        dragon.setDead();
    }
    
    /**
     * Sets a new life stage for the dragon.
     * 
     * @param lifeStage
     */
    public final void setLifeStage(EnumDragonLifeStage lifeStage) {
        L.trace("setLifeStage({})", lifeStage);
        if (dragon.isServer()) {
            ticksSinceCreationServer = lifeStage.startTicks();
            dataWatcher.set(dataParam, ticksSinceCreationServer);
        } else {
            L.error("setLifeStage called on Client");
        }
        updateLifeStage();
    }
    
    /**
     * Called when the dragon enters a new life stage.
     */ 
    private void onNewLifeStage(EnumDragonLifeStage lifeStage, EnumDragonLifeStage prevLifeStage) {
        L.trace("onNewLifeStage({},{})", prevLifeStage, lifeStage);
        
        if (dragon.isClient()) {
            // play particle and sound effects when the dragon hatches
            if (prevLifeStage != null && prevLifeStage == EGG && lifeStage == HATCHLING) {
                playEggCrackEffect();
                dragon.playSound(SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 1, 1);
            }
        } else {
            // update AI
            dragon.getBrain().updateAITasks();
            
            // update attribute modifier
            applyEntityAttributes();

            // heal dragon to updated full health
            dragon.setHealth(dragon.getMaxHealth());
        }
    }

    @Override
    public void onLivingUpdate() {
        // if the dragon is not an adult, update its growth ticks
        if (dragon.isServer()) {
            if (!isAdult()) {
                ticksSinceCreationServer++;
                if (ticksSinceCreationServer % TICKS_SINCE_CREATION_UPDATE_INTERVAL == 0){
                    dataWatcher.set(dataParam, ticksSinceCreationServer);
                }
            }
        } else {
            ticksSinceCreationClient.updateFromServer(dataWatcher.get(dataParam));
            if (!isAdult()) {
                ticksSinceCreationClient.tick();
            }
        }

        updateLifeStage();
        updateEgg();
        updateScale();
    }
    
    private void updateLifeStage() {
        // trigger event when a new life stage was reached
        EnumDragonLifeStage lifeStage = getLifeStage();
        if (lifeStagePrev != lifeStage) {
            onNewLifeStage(lifeStage, lifeStagePrev);
            lifeStagePrev = lifeStage;
        }
    }
    
    private void updateEgg() {
        if (!isEgg()) {
            return;
        }

        // animate egg wiggle based on the time the eggs take to hatch
        float progress = EnumDragonLifeStage.progressFromTickCount(getTicksSinceCreation());

        // wait until the egg is nearly hatched
        if (progress > EGG_WIGGLE_THRESHOLD) {
            float wiggleChance = (progress - EGG_WIGGLE_THRESHOLD) / EGG_WIGGLE_BASE_CHANCE * (1 - EGG_WIGGLE_THRESHOLD);

            if (eggWiggleX > 0) {
                eggWiggleX--;
            } else if (rand.nextFloat() < wiggleChance) {
                eggWiggleX = rand.nextBoolean() ? 10 : 20;
                if (progress > EGG_CRACK_THRESHOLD) {
                    playEggCrackEffect();
                }
            }

            if (eggWiggleZ > 0) {
                eggWiggleZ--;
            } else if (rand.nextFloat() < wiggleChance) {
                eggWiggleZ = rand.nextBoolean() ? 10 : 20;
                if (progress > EGG_CRACK_THRESHOLD) {
                    playEggCrackEffect();
                }
            }
        }

        // spawn generic particles
        double px = dragon.posX + (rand.nextDouble() - 0.5);
        double py = dragon.posY + (rand.nextDouble() - 0.5);
        double pz = dragon.posZ + (rand.nextDouble() - 0.5);
        double ox = (rand.nextDouble() - 0.5) * 2;
        double oy = (rand.nextDouble() - 0.5) * 2;
        double oz = (rand.nextDouble() - 0.5) * 2;
        dragon.worldObj.spawnParticle(EnumParticleTypes.PORTAL, px, py, pz, ox, oy, oz);
    }

    private void updateScale() {
        dragon.setScalePublic(getScale());
    }
    
    @Override
    public void onDeath() {
        if (dragon.isClient() && isEgg()) {
            playEggCrackEffect();
        }
    }
    
    public boolean isEgg() {
        return getLifeStage() == EGG;
    }
    
    public boolean isHatchling() {
        return getLifeStage() == HATCHLING;
    }
    
    public boolean isJuvenile() {
        return getLifeStage() == JUVENILE;
    }
    
    public boolean isAdult() {
        return getLifeStage() == ADULT;
    }
}
