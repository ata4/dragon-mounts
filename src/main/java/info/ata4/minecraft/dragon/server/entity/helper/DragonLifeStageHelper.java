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
import info.ata4.minecraft.dragon.server.entity.ai.air.EntityAICatchOwnerAir;
import info.ata4.minecraft.dragon.server.entity.ai.air.EntityAILand;
import info.ata4.minecraft.dragon.server.entity.ai.air.EntityAIRideAir;
import info.ata4.minecraft.dragon.server.entity.ai.ground.*;
import info.ata4.minecraft.dragon.server.util.ClientServerSynchronisedTickCount;
import info.ata4.minecraft.dragon.server.util.EntityClassPredicate;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static info.ata4.minecraft.dragon.server.entity.helper.EnumDragonLifeStage.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonLifeStageHelper extends DragonHelper {
    
    private static final Logger L = LogManager.getLogger();
    
    private static final String NBT_TICKS_SINCE_CREATION = "TicksSinceCreation";
    private static final int TICKS_SINCE_CREATION_UPDATE_INTERVAL = 100;
    
    private EnumDragonLifeStage lifeStagePrev;
    private final DragonScaleModifier scaleModifier = new DragonScaleModifier();
    private int eggWiggleX;
    private int eggWiggleZ;
    
    // the ticks since creation is used to control the dragon's life stage.  It is only updated by the server occasionally.
    // the client keeps a cached copy of it and uses client ticks to interpolate in the gaps.
    // when the watcher is updated from the server, the client will tick it faster or slower to resynchronise
    private final int dataIndexTicksSinceCreation;
    private int ticksSinceCreationServer;
    private final ClientServerSynchronisedTickCount ticksSinceCreationClient;

    public DragonLifeStageHelper(EntityTameableDragon dragon, int dataWatcherIndex) {
        super(dragon);
        dataIndexTicksSinceCreation = dataWatcherIndex;

        ticksSinceCreationServer = 0;
        dataWatcher.addObject(dataIndexTicksSinceCreation, ticksSinceCreationServer);
        ticksSinceCreationClient = new ClientServerSynchronisedTickCount(TICKS_SINCE_CREATION_UPDATE_INTERVAL);
        ticksSinceCreationClient.reset(ticksSinceCreationServer);
    }
    
    @Override
    public void applyEntityAttributes() {
        scaleModifier.setScale(getScale());

        dragon.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(scaleModifier);
        dragon.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(scaleModifier);
    }
    
    /**
     * Generates some egg shell particles and a breaking sound.
     */
    public void playEggCrackEffect() {
        dragon.worldObj.playAuxSFX(2001, dragon.getPosition(),
                Block.getIdFromBlock(Blocks.dragon_egg));
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
        return EnumDragonLifeStage.getLifeStageFromTickCount(age);
    }
    
    /**
     * Returns the size multiplier for the current age.
     * 
     * @return size
     */
//    public float getScale() {
//        int age = getTicksSinceCreation();
//        return DragonLifeStage.getScaleFromTickCount(age);
//    }

    public int getTicksSinceCreation() {
        if (!dragon.worldObj.isRemote) {
            return ticksSinceCreationServer;
        }
        return ticksSinceCreationClient.getCurrentTickCount();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger(NBT_TICKS_SINCE_CREATION, getTicksSinceCreation());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        int ticksRead = nbt.getInteger(NBT_TICKS_SINCE_CREATION);
        ticksRead = EnumDragonLifeStage.clipTickCountToValid(ticksRead);
        ticksSinceCreationServer = ticksRead;
        dataWatcher.updateObject(dataIndexTicksSinceCreation, ticksSinceCreationServer);
    }
    
    /**
     * Returns the size multiplier for the current age.
     * 
     * @return size
     */
    public float getScale() {
        EnumDragonLifeStage lifeStage = getLifeStage();
        int stageStartTicks = lifeStage.startTicks;
        int timeInThisStage = getTicksSinceCreation() - stageStartTicks;
        float fractionOfStage = timeInThisStage / (float) lifeStage.durationTicks;
        fractionOfStage = MathHelper.clamp_float(fractionOfStage, 0.0F, 1.0F);

        final float EGG_SIZE = 0.9F / EntityTameableDragon.BASE_WIDTH;
        final float HATCHLING_SIZE = 0.33F;
        final float JUVENILE_SIZE = 0.66F;
        final float ADULT_SIZE = 1.0F;

        switch (getLifeStage()) {
            case EGG: { // constant size for egg stage
                return EGG_SIZE;
            }
            case HATCHLING: {
                return HATCHLING_SIZE + fractionOfStage * (JUVENILE_SIZE - HATCHLING_SIZE);
            }
            case JUVENILE: {
                return JUVENILE_SIZE + fractionOfStage * (ADULT_SIZE - JUVENILE_SIZE);
            }
            case ADULT: {
                return ADULT_SIZE;
            }
            default: {
                L.error("Illegal lifestage in getScale():" + getLifeStage());
                return 1;
            }
        }
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
            ticksSinceCreationServer = lifeStage.startTicks;
            dataWatcher.updateObject(dataIndexTicksSinceCreation, ticksSinceCreationServer);
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
            if (prevLifeStage != null && prevLifeStage == EGG && lifeStage == HATCHLING) {
                playEggCrackEffect();
            }
        } else {
            // eggs and hatchlings can't fly
            dragon.setCanFly(lifeStage != EGG && lifeStage != HATCHLING);

//            // only hatchlings are small enough for doors
//            // (eggs don't move on their own anyway and are ignored)
//            dragon.getNavigator().setEnterDoors(lifeStage == HATCHLING);
            // guessed, based on EntityAIRestrictOpenDoor - break the door down, don't open it
            if (dragon.getNavigator() instanceof PathNavigateGround) {
                PathNavigateGround pathNavigateGround = (PathNavigateGround) dragon.getNavigator();
                pathNavigateGround.setEnterDoors(lifeStage == HATCHLING);
            }

            // update AI states so the egg won't move
//            dragon.setNoAI(lifeStage == EGG);  stops egg from sitting on the ground properly :(
            changeAITasks(lifeStage, prevLifeStage);

            // update attribute modifier
            IAttributeInstance healthAttrib = dragon.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            IAttributeInstance damageAttrib = dragon.getEntityAttribute(SharedMonsterAttributes.attackDamage);

            // remove old size modifiers
            healthAttrib.removeModifier(scaleModifier);
            damageAttrib.removeModifier(scaleModifier);

            // update modifier
            scaleModifier.setScale(getScale());

            // set new size modifiers
            healthAttrib.applyModifier(scaleModifier);
            damageAttrib.applyModifier(scaleModifier);

            // heal dragon to updated full health
            dragon.setHealth(dragon.getMaxHealth());
        }
    }

    @Override
    public void onLivingUpdate() {
        // testing code
//        if (dragon.isServer()) {
//            dragon.setGrowingAge((int) ((((Math.sin(Math.toRadians(dragon.ticksExisted))) + 1) * 0.5) * EGG.ageLimit));
//        }

        // if the dragon is not an adult, update its growth ticks
        if (!dragon.worldObj.isRemote) {
          if (getLifeStage() != ADULT) {
            ++ticksSinceCreationServer;
            if (ticksSinceCreationServer % TICKS_SINCE_CREATION_UPDATE_INTERVAL == 0){
              dataWatcher.updateObject(dataIndexTicksSinceCreation, ticksSinceCreationServer);
            }
          }
        } else {
            ticksSinceCreationClient.updateFromServer(dataWatcher.getWatchableObjectInt(
                    dataIndexTicksSinceCreation));
            if (getLifeStage() != ADULT) {
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
        int age = getTicksSinceCreation();
        int hatchAge = EnumDragonLifeStage.HATCHLING.durationTicks;
        int hatchTimeLeft = hatchAge - age;
        int hatchThreshold = hatchAge / 10;

        // wait until the egg is nearly hatched
        if (hatchTimeLeft < hatchThreshold) {
            float wiggleChance = hatchTimeLeft / (float) (hatchThreshold * 60);

            if (eggWiggleX > 0) {
                eggWiggleX--;
            } else if (rand.nextFloat() < wiggleChance) {
                eggWiggleX = rand.nextBoolean() ? 10 : 20;
                playEggCrackEffect();
            }

            if (eggWiggleZ > 0) {
                eggWiggleZ--;
            } else if (rand.nextFloat() < wiggleChance) {
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

    private void changeAITasks(EnumDragonLifeStage newLifeStage, EnumDragonLifeStage previousLifeStage) {
        // handle initialisation after load from NBT
        if (newLifeStage != null && previousLifeStage != null) {
            if (newLifeStage == previousLifeStage) {
                return;
            }
            if (newLifeStage != EGG && previousLifeStage != EGG) {
                return;
            }
        }

        EntityAITasks tasks = dragon.tasks;
        EntityAITasks airTasks = dragon.airTasks;
        EntityAITasks targetTasks = dragon.targetTasks;

        while (!tasks.taskEntries.isEmpty()) {
            EntityAIBase entityAIBase = ((EntityAITasks.EntityAITaskEntry) tasks.taskEntries.get(0)).action;
            tasks.removeTask(entityAIBase);
        }
        while (!airTasks.taskEntries.isEmpty()) {
            EntityAIBase entityAIBase = ((EntityAITasks.EntityAITaskEntry) airTasks.taskEntries.get(0)).action;
            airTasks.removeTask(entityAIBase);
        }
        while (!targetTasks.taskEntries.isEmpty()) {
            EntityAIBase entityAIBase = ((EntityAITasks.EntityAITaskEntry) targetTasks.taskEntries.get(0)).action;
            targetTasks.removeTask(entityAIBase);
        }

        if (newLifeStage == EGG) {
            return;
        }

        // mutex 1: movement
        // mutex 2: looking
        // mutex 4: special state
        tasks.addTask(0, new EntityAICatchOwnerGround(dragon)); // mutex all
        tasks.addTask(1, new EntityAIRideGround(dragon, 1)); // mutex all
        tasks.addTask(2, new EntityAISwimming(dragon)); // mutex 4
        tasks.addTask(3, dragon.getAISit()); // mutex 4+1
        tasks.addTask(4, new EntityAIDragonMate(dragon, 0.6)); // mutex 2+1
        tasks.addTask(5, new EntityAITempt(dragon, 0.75, dragon.FAVORITE_FOOD, false)); // mutex 2+1
        tasks.addTask(6, new EntityAIAttackOnCollide(dragon, 1, true)); // mutex 2+1
        tasks.addTask(7, new EntityAIFollowParent(dragon, 0.8)); // mutex 2+1
        tasks.addTask(8, new EntityAIDragonFollowOwner(dragon, 1, 12, 128)); // mutex 2+1
        tasks.addTask(8, new EntityAIPanicChild(dragon, 1)); // mutex 1
        tasks.addTask(9, new EntityAIWander(dragon, 1)); // mutex 1
        tasks.addTask(10, new EntityAIWatchIdle(dragon)); // mutex 2
        tasks.addTask(10, new EntityAIWatchLiving(dragon, 16, 0.05f)); // mutex 2

        // mutex 1: waypointing
        // mutex 2: continuous waypointing
        airTasks.addTask(0, new EntityAIRideAir(dragon)); // mutex all
        airTasks.addTask(0, new EntityAILand(dragon)); // mutex 0
        airTasks.addTask(0, new EntityAICatchOwnerAir(dragon)); // mutex all

        // mutex 1: generic targeting
        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(dragon)); // mutex 1
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(dragon)); // mutex 1
        targetTasks.addTask(3, new EntityAIHurtByTarget(dragon, false)); // mutex 1
        targetTasks.addTask(4, new EntityAIHunt(dragon, EntityAnimal.class, false,
                new EntityClassPredicate(
                        EntitySheep.class,
                        EntityPig.class,
                        EntityChicken.class,
                        EntityRabbit.class
                )
        )); // mutex 1
    }

}
