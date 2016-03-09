/*
 ** 2013 March 23
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.DragonBreed;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.EnumUtils;

/**
 * Helper class for breed properties.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedHelper extends DragonHelper {
    
    private static final Logger L = LogManager.getLogger();
    private static final int BLOCK_RANGE = 2;
    private static final String NBT_BREED = "Breed";
    private static final String NBT_BREED_POINTS = "breedPoints";

    private final int dataIndex;
    private final Map<EnumDragonBreed, AtomicInteger> breedPoints = new EnumMap<>(EnumDragonBreed.class);
    
    public DragonBreedHelper(EntityTameableDragon dragon, int dataIndex) {
        super(dragon);
        
        this.dataIndex = dataIndex;

        if (dragon.isServer()) {
            // initialize map to avoid future checkings
            for (EnumDragonBreed type : EnumDragonBreed.values()) {
                // default breed has initial points
                int startPoints = type == EnumDragonBreed.DEFAULT ? 100 : 0;
                breedPoints.put(type, new AtomicInteger(startPoints));
            }
        }
        
        dataWatcher.addObject(dataIndex, EnumDragonBreed.DEFAULT.getName());
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString(NBT_BREED, getBreedType().getName());
        
        NBTTagCompound breedPointTag = new NBTTagCompound();
        breedPoints.forEach((type, points) -> {
            breedPointTag.setInteger(type.getName(), points.get());
        });
        nbt.setTag(NBT_BREED_POINTS, breedPointTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        // read breed name and convert it to the corresponding breed object
        String breedName = nbt.getString(NBT_BREED);
        //setBreedType(EnumDragonBreed.fromName(breedName));
        
        EnumDragonBreed newBreed = EnumUtils.getEnum(EnumDragonBreed.class, breedName.toUpperCase());
        if (newBreed == null) {
            newBreed = EnumDragonBreed.DEFAULT;
            L.warn("Dragon {} loaded with invalid breed type {}, using {} instead",
                    dragon.getEntityId(), breedName, newBreed);
        }
        
        // read breed points
        NBTTagCompound breedPointTag = nbt.getCompoundTag(NBT_BREED_POINTS);
        breedPoints.forEach((type, points) -> {
            points.set(breedPointTag.getInteger(type.getName()));
        });
    }
    
    public Map<EnumDragonBreed, AtomicInteger> getBreedPoints() {
        return Collections.unmodifiableMap(breedPoints);
    }
    
    public EnumDragonBreed getBreedType() {
        String breedName = dataWatcher.getWatchableObjectString(dataIndex);
        return EnumUtils.getEnum(EnumDragonBreed.class, breedName.toUpperCase());
    }
    
    public void setBreedType(EnumDragonBreed newType) {
        L.trace("setBreed({})", newType);
        
        if (newType == null) {
            throw new NullPointerException();
        }
        
        // ignore breed changes on client side, it's controlled by the server
        if (dragon.isClient()) {
            return;
        }
        
        // check if the breed actually changed
        EnumDragonBreed oldType = getBreedType();
        if (oldType == newType) {
            return;
        }
        
        DragonBreed oldBreed = oldType.getBreed();
        DragonBreed newBreed = newType.getBreed();
        
        // switch breed stats
        oldBreed.onDisable(dragon);
        newBreed.onEnable(dragon);
        
        // check for fire immunity and disable fire particles
        dragon.setImmuneToFire(newBreed.isImmuneToDamage(DamageSource.inFire)
                || newBreed.isImmuneToDamage(DamageSource.onFire)
                || newBreed.isImmuneToDamage(DamageSource.lava));
        
        // update breed name
        dataWatcher.updateObject(dataIndex, newType.getName());
    }
    
    @Override
    public void onLivingUpdate() {
        EnumDragonBreed currentType = getBreedType();
        
        if (dragon.isEgg()) {
            // spawn breed-specific particles every other tick
            if (dragon.isClient() && dragon.ticksExisted % 2 == 0) {
                if (currentType != EnumDragonBreed.DEFAULT) {
                    double px = dragon.posX + (rand.nextDouble() - 0.5);
                    double py = dragon.posY + (rand.nextDouble() - 0.5);
                    double pz = dragon.posZ + (rand.nextDouble() - 0.5);
                    DragonBreed current = currentType.getBreed();
                    dragon.worldObj.spawnParticle(EnumParticleTypes.REDSTONE, px, py + 1, pz,
                            current.getColorR(), current.getColorG(), current.getColorB());
                }
            }

            // update egg breed every second on the server
            if (dragon.isServer() && dragon.ticksExisted % 20 == 0) {
                BlockPos eggPos = dragon.getPosition();
                
                // scan surrounding for breed-loving blocks
                BlockPos eggPosFrom = eggPos.add(BLOCK_RANGE, BLOCK_RANGE, BLOCK_RANGE);
                BlockPos eggPosTo = eggPos.add(-BLOCK_RANGE, -BLOCK_RANGE, -BLOCK_RANGE);
                
                BlockPos.getAllInBoxMutable(eggPosFrom, eggPosTo).forEach(blockPos -> {
                    Block block = dragon.worldObj.getBlockState(blockPos).getBlock();
                    breedPoints.entrySet().stream()
                        .filter(breed -> (breed.getKey().getBreed().isHabitatBlock(block)))
                        .forEach(breed -> breed.getValue().incrementAndGet());
                });

                // check biome
                BiomeGenBase biome = dragon.worldObj.getBiomeGenForCoords(eggPos);

                breedPoints.keySet().forEach(breed -> {
                    // check for biomes
                    if (breed.getBreed().isHabitatBiome(biome)) {
                        breedPoints.get(breed).incrementAndGet();
                    }

                    // extra points for good environments
                    if (breed.getBreed().isHabitatEnvironment(dragon)) {
                        breedPoints.get(breed).addAndGet(3);
                    }
                });

                // update most dominant breed
                EnumDragonBreed newType = breedPoints.entrySet().stream()
                    .max((breed1, breed2) -> Integer.compare(
                            breed1.getValue().get(),
                            breed2.getValue().get()))
                    .get().getKey();
                
                if (newType != currentType) {
                    setBreedType(newType);
                }
            }
        }
        
        currentType.getBreed().onUpdate(dragon);
    }

    @Override
    public void onDeath() {
        getBreedType().getBreed().onDeath(dragon);
    }
    
    public void inheritBreed(EntityTameableDragon parent1, EntityTameableDragon parent2) {
        breedPoints.get(parent1.getBreedType()).addAndGet(1800 + rand.nextInt(1800));
        breedPoints.get(parent2.getBreedType()).addAndGet(1800 + rand.nextInt(1800));
    }
}
