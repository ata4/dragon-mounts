/*
 ** 2012 August 13
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity;

import info.ata4.minecraft.dragon.client.model.anim.DragonAnimator;
import info.ata4.minecraft.dragon.server.entity.ai.path.PathNavigateFlying;
import info.ata4.minecraft.dragon.server.entity.breeds.DragonBreed;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.*;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import static net.minecraft.entity.SharedMonsterAttributes.*;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Here be dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityTameableDragon extends EntityTameable {
    
    private static final Logger L = LogManager.getLogger();
    
    public static final IAttribute MOVE_SPEED_AIR = new RangedAttribute(null,
        "generic.movementSpeedAir", 1.5, 0.0, Double.MAX_VALUE)
            .setDescription("Movement Speed Air")
            .setShouldWatch(true);
    
    
    // base attributes
    public static final double BASE_SPEED_GROUND = 0.3;
    public static final double BASE_SPEED_AIR = 0.4;
    public static final double BASE_DAMAGE = 8;
    public static final double BASE_HEALTH = 60;
    public static final float BASE_WIDTH = 2.75f;
    public static final float BASE_HEIGHT = 2.75f;
    public static final double BASE_FOLLOW_RANGE = 16;
    public static final double BASE_FOLLOW_RANGE_FLYING = BASE_FOLLOW_RANGE * 2;
    public static final int HOME_RADIUS = 64;
    public static final double ALTITUDE_FLYING_THRESHOLD = 2;

    // data value IDs
    private static final int INDEX_FLYING = 18;
    private static final int INDEX_SADDLED = 20;
    private static final int INDEX_BREEDER = 21;
    private static final int INDEX_BREED = 22;
    private static final int INDEX_REPRO_COUNT = 23;
    private static final int INDEX_TICKS_SINCE_CREATION = 24;
    
    // data NBT IDs
    private static final String NBT_SADDLED = "Saddle";

    // server/client delegates
    private final Map<Class, DragonHelper> helpers = new HashMap<>();
    
    // client-only delegates
    private final DragonBodyHelper bodyHelper = new DragonBodyHelper(this);
    
    public EntityTameableDragon(World world) {
        super(world);
        
        // set base size
        setSize(BASE_WIDTH, BASE_HEIGHT);
        
        // enables walking over blocks
        stepHeight = 1;
        
        // create entity delegates
        addHelper(new DragonBreedHelper(this, INDEX_BREED));
        addHelper(new DragonLifeStageHelper(this, INDEX_TICKS_SINCE_CREATION));
        addHelper(new DragonReproductionHelper(this, INDEX_BREEDER, INDEX_REPRO_COUNT));
        addHelper(new DragonSoundManager(this));
        addHelper(new DragonInteractHelper(this));
        
        if (isClient()) {
            addHelper(new DragonParticleHelper(this));
            addHelper(new DragonAnimator(this));
        } else {
            addHelper(new DragonBrain(this));
        }
        
        moveHelper = new DragonMoveHelper(this);
        
        // init helpers
        helpers.values().forEach(DragonHelper::applyEntityAttributes);
    }
    
    @Override
    protected float func_110146_f(float p_110146_1_, float p_110146_2_) {
        // required to fixate body while sitting. also slows down rotation while
        // standing.
        bodyHelper.updateRenderAngles();
        return p_110146_2_;
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
        
        dataWatcher.addObject(INDEX_FLYING, (byte) 0);
        dataWatcher.addObject(INDEX_SADDLED, (byte) 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        
        getAttributeMap().registerAttribute(attackDamage);
        getAttributeMap().registerAttribute(MOVE_SPEED_AIR);
        
        getEntityAttribute(movementSpeed).setBaseValue(BASE_SPEED_GROUND);
        getEntityAttribute(MOVE_SPEED_AIR).setBaseValue(BASE_SPEED_AIR);
        getEntityAttribute(maxHealth).setBaseValue(BASE_HEALTH);
        getEntityAttribute(attackDamage).setBaseValue(BASE_DAMAGE);
        getEntityAttribute(followRange).setBaseValue(BASE_FOLLOW_RANGE);
    }
    
    /**
     * Returns true if the dragon is saddled.
     */
    public boolean isSaddled() {
        return getBooleanData(INDEX_SADDLED);
    }

    /**
     * Set or remove the saddle of the dragon.
     */
    public void setSaddled(boolean saddled) {
        L.trace("setSaddled({})", saddled);
        setBooleanData(INDEX_SADDLED, saddled);
    }
    
    public boolean canFly() {
        // eggs and hatchlings can't fly
        return !isEgg() && !isHatchling();
    }
    
    /**
     * Returns true if the entity is flying.
     */
    public boolean isFlying() {
        return getBooleanData(INDEX_FLYING);
    }
    
    /**
     * Set the flying flag of the entity.
     */
    public void setFlying(boolean flying) {
        L.trace("setFlying({})", flying);
        setBooleanData(INDEX_FLYING, flying);
    }
    
    /**
     * Returns the distance to the ground while the entity is flying.
     */
    public double getAltitude() {
        BlockPos groundPos = worldObj.getHeight(getPosition());
        return posY - groundPos.getY();
    }
    
    /**
     * Causes this entity to lift off if it can fly.
     */
    public void liftOff() {
        L.trace("liftOff");
        if (canFly()) {
            jump();
        }
    }
    
    @Override
    protected float getJumpUpwardsMotion() {
        // stronger jumps for easier lift-offs
        return canFly() ? 1 : super.getJumpUpwardsMotion();
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    @Override
    public void fall(float distance, float damageMultiplier) {
        // ignore fall damage if the entity can fly
        if (!canFly()) {
            super.fall(distance, damageMultiplier);
        }
    }
    
     /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean(NBT_SADDLED, isSaddled());
        
        helpers.values().forEach(helper -> helper.writeToNBT(nbt));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        setSaddled(nbt.getBoolean(NBT_SADDLED));
        
        helpers.values().forEach(helper -> helper.readFromNBT(nbt));
    }
    
    @Override
    public void onLivingUpdate() {
        helpers.values().forEach(DragonHelper::onLivingUpdate);
        
        if (isServer()) {
            // set home position near owner when tamed
            if (isTamed()) {
                Entity owner = getOwner();
                if (owner != null) {
                    setHomePosAndDistance(owner.getPosition(), HOME_RADIUS);
                }
            }

            // update flying state based on the distance to the ground
            boolean flying = canFly() && getAltitude() > ALTITUDE_FLYING_THRESHOLD;
            if (flying != isFlying()) {
                // notify client
                setFlying(flying);
                
                // clear tasks (needs to be done before switching the navigator!)
                getBrain().clearTasks();
                
                // update AI follow range (needs to be updated before creating 
                // new PathNavigate!)
                getEntityAttribute(SharedMonsterAttributes.followRange)
                    .setBaseValue(flying ? BASE_FOLLOW_RANGE_FLYING : BASE_FOLLOW_RANGE);
                
                // update pathfinding method
                if (flying) {
                    navigator = new PathNavigateFlying(this, worldObj);
                } else {
                    navigator = new PathNavigateGround(this, worldObj);
                }
                
                // tasks need to be updated after switching modes
                getBrain().updateAITasks();
            }
        }
        
        super.onLivingUpdate();
    }
    
    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        // disable method while flying, the movement is done entirely by
        // moveEntity() and this one just makes the dragon to fall slowly when
        // hovering
        if (!isFlying()) {
            super.moveEntityWithHeading(strafe, forward);
        }
    }
    
    /**
     * Handles entity death timer, experience orb and particle creation
     */
    @Override
    protected void onDeathUpdate() {
        helpers.values().forEach(DragonHelper::onDeathUpdate);
        
        // unmount any riding entity
        if (riddenByEntity != null) {
            riddenByEntity.mountEntity(null);
        }
                
        // freeze at place
        motionX = motionY = motionZ = 0;
        rotationYaw = prevRotationYaw;
        rotationYawHead = prevRotationYawHead;
        
        if (isEgg()) {
            setDead();
        } else {
            // actually delete entity after the time is up
            if (deathTime >= getMaxDeathTime()) {
                setDead();
            }
        }
        
        deathTime++;
    }
    
    @Override
    public void setDead() {
        helpers.values().forEach(DragonHelper::onDeath);
        super.setDead();
    }

    @Override
    public String getName() {
        // return custom name if set
        if (hasCustomName()) {
            return getCustomNameTag();
        }
        
        // return default breed name otherwise
        String entName = EntityList.getEntityString(this);
        String breedName = getBreedType().getName().toLowerCase();
        return StatCollector.translateToLocal("entity." + entName + "." + breedName + ".name");
    }
    
    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound() {
        return getSoundManager().getLivingSound();
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        return getSoundManager().getHurtSound();
    }
    
    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound() {
        return getSoundManager().getDeathSound();
    }
    
    /**
     * Plays living's sound at its position
     */
    @Override
    public void playLivingSound() {
        getSoundManager().playLivingSound();
    }
    
    @Override
    public void playSound(String name, float volume, float pitch) {
        getSoundManager().playSound(name, volume, pitch);
    }
    
    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void playStepSound(BlockPos entityPos, Block block) {
        getSoundManager().playStepSound(entityPos, block);
    }
    
    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume() {
        // note: unused, managed in playSound()
        return 1;
    }
    
    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
    protected float getSoundPitch() {
        // note: unused, managed in playSound()
        return 1;
    }
    
    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    @Override
    public int getTalkInterval() {
        return getSoundManager().getTalkInterval();
    }
    
    /**
     * Get this Entity's EnumCreatureAttribute
     */
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return getBreed().getCreatureAttribute();
    }
    
    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack playerItem = player.inventory.getCurrentItem();
        
        // duplicate dragon with entity egg
        if (playerItem != null && playerItem.getItem() == Items.spawn_egg) {
            return super.interact(player);
        }
        
        // don't interact with eggs!
        if (isEgg()) {
            return false;
        }
        
        // inherited interaction
        if (super.interact(player)) {
            return true;
        }
        
        return getInteractHelper().interact(player, playerItem);
    }
    
    public void tamedFor(EntityPlayer player, boolean successful) {
        if (successful) {
            setTamed(true);
            navigator.clearPathEntity();  // replacement for setPathToEntity(null);
            setAttackTarget(null);
            setOwnerId(player.getUniqueID().toString());
            playTameEffect(true);
            worldObj.setEntityState(this, (byte) 7);
        } else {
            playTameEffect(false);
            worldObj.setEntityState(this, (byte) 6);
        }
    }
    
    public boolean isTamedFor(EntityPlayer player) {
        return isTamed() && isOwner(player);
    }    
    
    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    @Override
    public boolean isBreedingItem(ItemStack item) {
        return getBreed().getBreedingItem() == item.getItem();
    }
    
    /**
     * Returns the height of the eyes. Used for looking at other entities.
     */
    @Override
    public float getEyeHeight() {
        float eyeHeight = super.getEyeHeight();

        if (isSitting()) {
            eyeHeight *= 0.8f;
        }

        return eyeHeight;
    }
    
    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
    public double getMountedYOffset() {
        return (isSitting() ? 1.7f : 2.2f) * getScale();
    }
    
    /**
     * Returns render size modifier
     */
    @Override
    public float getRenderSizeModifier() {
        return getScale();
    }
    
    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed() {
        return super.canBePushed() && isEgg();
    }
    
    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn() {
        return false;
    }
    
    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean isOnLadder() {
        // this better doesn't happen...
        return false;
    }
    
    /**
     * Drop 0-2 items of this living's type.
     * @param par1 - Whether this entity has recently been hit by a player.
     * @param par2 - Level of Looting used to kill this mob.
     */
    @Override
    protected void dropFewItems(boolean par1, int par2) {
        super.dropFewItems(par1, par2);
        
        // drop saddle if equipped
        if (isSaddled()) {
            dropItem(Items.saddle, 1);
        }
    }
    
    @Override
    public boolean attackEntityAsMob(Entity victim) {
        // code copied from EntityMob::attackEntityAsMob
        float attackDamageValue = (float) getEntityAttribute(attackDamage).getAttributeValue();
        int knockback = 0;

        if (victim instanceof EntityLivingBase) {
            attackDamageValue += EnchantmentHelper.func_152377_a(getHeldItem(),
                    ((EntityLivingBase) victim).getCreatureAttribute());
            knockback += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean attacked = victim.attackEntityFrom(DamageSource.causeMobDamage(this),
                attackDamageValue);

        if (attacked) {
            if (knockback > 0) {
                double vx = -Math.sin(Math.toRadians(rotationYaw)) * knockback * 0.5;
                double vy = 0.1;
                double vz = Math.cos(Math.toRadians(rotationYaw)) * knockback * 0.5;
                victim.addVelocity(vx, vy, vz);

                motionX *= 0.6;
                motionZ *= 0.6;
            }

            int fireAspect = EnchantmentHelper.getFireAspectModifier(this);

            if (fireAspect > 0) {
                victim.setFire(fireAspect * 4);
            }

            applyEnchantments(this, victim);

            setLastAttacker(victim);

            // play eating sound
            playSound(getSoundManager().getAttackSound(), 1, 0.7f);

            if (worldObj instanceof WorldServer) {
                ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(
                        this, new S0BPacketAnimation(this, 0));
            }

        }

        return attacked;
    }
    
    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource src, float par2) {
        if (isInvulnerableTo(src)) {
            return false;
        }
        
        // don't just sit there!
        aiSit.setSitting(false);
        
        return super.attackEntityFrom(src, par2);
    }
    
    /**
     * Return whether this entity should be rendered as on fire.
     */
    @Override
    public boolean canRenderOnFire() {
        return super.canRenderOnFire() && !getBreed().isImmuneToDamage(DamageSource.inFire);
    }
    
    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMateWith(EntityAnimal mate) {
        return getReproductionHelper().canMateWith(mate);
    }
    
    /**
     * This function is used when two same-species animals in 'love mode' breed to generate the new baby animal.
     */
    @Override
    public EntityAgeable createChild(EntityAgeable mate) {
        return getReproductionHelper().createChild(mate);
    }
    
    private void addHelper(DragonHelper helper) {
        L.trace("addHelper({})", helper.getClass().getName());
        helpers.put(helper.getClass(), helper);
    }
    
    private <T extends DragonHelper> T getHelper(Class<T> clazz) {
        return (T) helpers.get(clazz);
    }

    public DragonBreedHelper getBreedHelper() {
        return getHelper(DragonBreedHelper.class);
    }
    
    public DragonLifeStageHelper getLifeStageHelper() {
        return getHelper(DragonLifeStageHelper.class);
    }
    
    public DragonReproductionHelper getReproductionHelper() {
        return getHelper(DragonReproductionHelper.class);
    }
    
    public DragonParticleHelper getParticleHelper() {
        return getHelper(DragonParticleHelper.class);
    }
    
    public DragonAnimator getAnimator() {
        return getHelper(DragonAnimator.class);
    }
    
    public DragonSoundManager getSoundManager() {
        return getHelper(DragonSoundManager.class);
    }
    
    public DragonBrain getBrain() {
        return getHelper(DragonBrain.class);
    }
    
    public DragonInteractHelper getInteractHelper() {
        return getHelper(DragonInteractHelper.class);
    }
    
    public boolean getBooleanData(int index) {
        return (dataWatcher.getWatchableObjectByte(index) & 1) != 0;
    }
    
    public void setBooleanData(int index, boolean value) {
        dataWatcher.updateObject(index, value ? (byte) 1 : (byte) 0);
    }
    
    /**
     * Returns the breed for this dragon.
     * 
     * @return breed
     */
    public EnumDragonBreed getBreedType() {
        return getBreedHelper().getBreedType();
    }
    
    /**
     * Sets the new breed for this dragon.
     * 
     * @param type new breed
     */
    public void setBreedType(EnumDragonBreed type) {
        getBreedHelper().setBreedType(type);
    }
    
    public DragonBreed getBreed() {
        return getBreedType().getBreed();
    }

    public EntityPlayer getRidingPlayer() {
        if (riddenByEntity instanceof EntityPlayer) {
            return (EntityPlayer) riddenByEntity;
        } else {
            return null;
        }
    }
    
    public void setRidingPlayer(EntityPlayer player) {
        L.trace("setRidingPlayer({})", player.getName());
        player.rotationYaw = rotationYaw;
        player.rotationPitch = rotationPitch;
        player.mountEntity(this);
    }
    
    @Override
    public void updateRiderPosition() {
        if (riddenByEntity != null) {
            double px = posX;
            double py = posY + getMountedYOffset() + riddenByEntity.getYOffset();
            double pz = posZ;
            
            // dragon position is the middle of the model and the saddle is on
            // the shoulders, so move player forwards on Z axis relative to the
            // dragon's rotation to fix that
            Vec3 pos = new Vec3(0, 0, 0.8 * getScale());
            pos = pos.rotateYaw((float) Math.toRadians(-renderYawOffset)); // oops
            px += pos.xCoord;
            py += pos.yCoord;
            pz += pos.zCoord;
                    
            riddenByEntity.setPosition(px, py, pz);
            
            // fix rider rotation
            if (riddenByEntity instanceof EntityLiving) {
                EntityLiving rider = ((EntityLiving) riddenByEntity);
                rider.prevRotationPitch = rider.rotationPitch;
                rider.prevRotationYaw = rider.rotationYaw;
                rider.renderYawOffset = renderYawOffset;
            }
        }
    }
    
    public boolean isInvulnerableTo(DamageSource src) {
        Entity srcEnt = src.getEntity();
        if (srcEnt != null) {
            // ignore own damage
            if (srcEnt == this) {
                return true;
            }
            
            // ignore damage from rider
            if (srcEnt == riddenByEntity) {
                return true;
            }
        }
        
        // don't drown as egg
        if (src.damageType.equals("drown") && isEgg()) {
            return true;
        }
        
        return getBreed().isImmuneToDamage(src);
    }
    
    /**
     * Returns the entity's health relative to the maximum health.
     * 
     * @return health normalized between 0 and 1
     */
    public double getHealthRelative() {
        return getHealth() / (double) getMaxHealth();
    }
    
    public int getDeathTime() {
        return deathTime;
    }
    
    public int getMaxDeathTime() {
        return 120;
    }
    
    public void setImmuneToFire(boolean isImmuneToFire) {
        L.trace("setImmuneToFire({})", isImmuneToFire);
        this.isImmuneToFire = isImmuneToFire;
    }
    
    public void setAttackDamage(double damage) {
        L.trace("setAttackDamage({})", damage);
        getEntityAttribute(attackDamage).setBaseValue(damage);
    }
    
    public double getAttackDamage() {
        return getEntityAttribute(attackDamage).getAttributeValue();
    }
    
    /**
     * Public wrapper for protected final setScale(), used by DragonLifeStageHelper.
     * 
     * @param scale 
     */
    public void setScalePublic(float scale) {
        double posXTmp = posX;
        double posYTmp = posY;
        double posZTmp = posZ;
        boolean onGroundTmp = onGround;
        
        setScale(scale);
        
        // workaround for a vanilla bug; the position is apparently not set correcty
        // after changing the entity size, causing asynchronous server/client positioning
        setPosition(posXTmp, posYTmp, posZTmp);
        
        // otherwise, setScale stops the dragon from landing while it is growing
        onGround = onGroundTmp;
    }
    
    /**
     * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
     * positive, it get's decremented each tick. Don't confuse this with EntityLiving.getAge. With a negative value the
     * Entity is considered a child.
     */
    @Override
    public int getGrowingAge() {
        // adapter for vanilla code to enable breeding interaction
        return isAdult() ? 0 : -1;
    }
    
    /**
     * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
     * positive, it get's decremented each tick. With a negative value the Entity is considered a child.
     */
    @Override
    public void setGrowingAge(int age) {
        // managed by DragonLifeStageHelper, so this is a no-op
    }
    
    /**
     * Sets the scale for an ageable entity according to the boolean parameter, which says if it's a child.
     */
    @Override
    public void setScaleForAge(boolean p_98054_1_) {
        // managed by DragonLifeStageHelper, so this is a no-op
    }
    
    /**
     * Returns the size multiplier for the current age.
     * 
     * @return scale
     */
    public float getScale() {
        return getLifeStageHelper().getScale();
    }
    
    public boolean isEgg() {
        return getLifeStageHelper().isEgg();
    }
    
    public boolean isHatchling() {
        return getLifeStageHelper().isHatchling();
    }
    
    public boolean isJuvenile() {
        return getLifeStageHelper().isJuvenile();
    }
    
    public boolean isAdult() {
        return getLifeStageHelper().isAdult();
    }
    
    @Override
    public boolean isChild() {
        return !isAdult();
    }
    
    /**
     * Checks if this entity is running on a client.
     * 
     * Required since MCP's isClientWorld returns the exact opposite...
     * 
     * @return true if the entity runs on a client or false if it runs on a server
     */
    public final boolean isClient() {
        return worldObj.isRemote;
    }
    
    /**
     * Checks if this entity is running on a server.
     * 
     * @return true if the entity runs on a server or false if it runs on a client
     */
    public final boolean isServer() {
        return !worldObj.isRemote;
    }
}
