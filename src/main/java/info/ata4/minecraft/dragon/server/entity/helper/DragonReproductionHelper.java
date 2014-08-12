/*
 ** 2013 October 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonReproductionHelper extends DragonHelper  {
    
    private static final Logger L = LogManager.getLogger();
    public static final String NBT_BREEDER = "HatchedBy";
    public static final String NBT_REPRODUCED = "HasReproduced";
    public static final String NBT_REPRO_COUNT = "ReproductionCount";
    
    public static final byte REPRO_LIMIT = 2;
    
    private final int dataIndexBreeder;
    private final int dataIndexReproduced;
    
    public DragonReproductionHelper(EntityTameableDragon dragon, int dataIndexBreeder, int dataIndexReproCount) {
        super(dragon);
        
        this.dataIndexBreeder = dataIndexBreeder;
        this.dataIndexReproduced = dataIndexReproCount;
        
        dataWatcher.addObject(dataIndexBreeder, "");
        dataWatcher.addObject(dataIndexReproCount, 0);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString(NBT_BREEDER, getBreederName() == null ? "" : getBreederName());
        nbt.setInteger(NBT_REPRO_COUNT, getReproCount());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        setBreederName(nbt.getString(NBT_BREEDER));
                
        // convert old NBT format
        int reproCount = 0;
        if (!nbt.hasKey(NBT_REPRO_COUNT)) {
            if (nbt.getBoolean(NBT_REPRODUCED)) {
                reproCount++;
            }
        } else {
            reproCount = nbt.getInteger(NBT_REPRO_COUNT);
        }
        
        setReproCount(reproCount);
    }
    
    public int getReproCount() {
        return dataWatcher.getWatchableObjectInt(dataIndexReproduced);
    }
    
    public void setReproCount(int reproCount) {
        L.trace("setReproCount({})", reproCount);
        dataWatcher.updateObject(dataIndexReproduced, reproCount);
    }
    
    public void addReproduced() {
        setReproCount(getReproCount() + 1);
    }
    
    public boolean canReproduce() {
        return getReproCount() < REPRO_LIMIT;
    }
    
    public String getBreederName() {
        return dataWatcher.getWatchableObjectString(dataIndexBreeder);
    }
    
    public EntityPlayer getBreeder() {
        String breederName = getBreederName();
        return dragon.worldObj.getPlayerEntityByName(breederName);
    }
    
    public void setBreederName(String breederName) {
        L.trace("setBreederName({})", breederName);
        dataWatcher.updateObject(dataIndexBreeder, breederName);
    }
    
    public boolean canMateWith(EntityAnimal mate) {
        if (mate == dragon) {
            // No. Just... no.
            return false;
        } else if (!dragon.isTamed()) {
            return false;
        } else if (!(mate instanceof EntityTameableDragon)) {
            return false;
        } else if (!canReproduce()) {
            return false;
        }
        
        EntityTameableDragon dragonMate = (EntityTameableDragon) mate;
        
        if (!dragonMate.isTamed()) {
            return false;
        } else if (!dragonMate.getReproductionHelper().canReproduce()) {
            return false;
        } else {
            return dragon.isInLove() && dragonMate.isInLove();
        }
    }
    
    public EntityAgeable createChild(EntityAgeable mate) {
        if (!(mate instanceof EntityTameableDragon)) {
            throw new IllegalArgumentException("The mate isn't a dragon");
        }
        
        EntityTameableDragon parent1 = dragon;
        EntityTameableDragon parent2 = (EntityTameableDragon) mate;
        EntityTameableDragon baby = new EntityTameableDragon(dragon.worldObj);

        // mix the custom names in case both parents have one
        if (parent1.hasCustomNameTag() && parent2.hasCustomNameTag()) {
            String p1Name = parent1.getCustomNameTag();
            String p2Name = parent2.getCustomNameTag();
            String babyName;

            if (p1Name.contains(" ") || p2Name.contains(" ")) {
                // combine two words with space
                // "Tempor Invidunt Dolore" + "Magna"
                // = "Tempor Magna" or "Magna Tempor"
                String[] p1Names = p1Name.split(" ");
                String[] p2Names = p2Name.split(" ");

                p1Name = fixChildName(p1Names[rand.nextInt(p1Names.length)]);
                p2Name = fixChildName(p2Names[rand.nextInt(p2Names.length)]);

                babyName = rand.nextBoolean() ? p1Name + " " + p2Name : p2Name + " " + p1Name;
            } else {
                // scramble two words
                // "Eirmod" + "Voluptua"
                // = "Eirvolu" or "Volueir" or "Modptua" or "Ptuamod" or ...
                if (rand.nextBoolean()) {
                    p1Name = p1Name.substring(0, (p1Name.length() - 1) / 2);
                } else {
                    p1Name = p1Name.substring((p1Name.length() - 1) / 2);
                }

                if (rand.nextBoolean()) {
                    p2Name = p2Name.substring(0, (p2Name.length() - 1) / 2);
                } else {
                    p2Name = p2Name.substring((p2Name.length() - 1) / 2);
                }

                p2Name = fixChildName(p2Name);

                babyName = rand.nextBoolean() ? p1Name + p2Name : p2Name + p1Name;
            }

            baby.setCustomNameTag(babyName);
        }
        
        // inherit the baby's breed from its parents
        baby.getBreedHelper().inheritBreed(parent1, parent2);
        
        // increase reproduction counter
        parent1.getReproductionHelper().addReproduced();
        parent2.getReproductionHelper().addReproduced();

        return baby;
    }
    
    private String fixChildName(String nameOld) {
        if (nameOld == null || nameOld.isEmpty()) {
            return nameOld;
        }
        
        // create all lower-case char array
        char[] chars = nameOld.toLowerCase().toCharArray();
        
        // convert first char to upper-case
        chars[0] = Character.toUpperCase(chars[0]);
        
        String nameNew = new String(chars);
        
        if (!nameOld.equals(nameNew)) {
            L.debug("Fixed child name {} -> {}");
        }
        
        return nameNew;
    }
}
