/*
 ** 2013 October 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedGhost extends DragonBreed {
    
    private final Map<EntityTameableDragon, EntityAIRestrictSun> appliedTasks = new HashMap<>();

    DragonBreedGhost(EnumDragonBreed type) {
        super(type, "undead", 0xbebebe);
        
        addImmunity(DamageSource.wither);
        
        addHabitatBlock(Blocks.web);
    }
    
    @Override
    public void onEnable(EntityTameableDragon dragon) {
        EntityAIRestrictSun restrictSun = new EntityAIRestrictSun(dragon);
        dragon.tasks.addTask(2, restrictSun);
        appliedTasks.put(dragon, restrictSun);
    }
    
    @Override
    public void onDisable(EntityTameableDragon dragon) {
        if (appliedTasks.containsKey(dragon)) {
            dragon.tasks.removeTask(appliedTasks.get(dragon));
            appliedTasks.remove(dragon);
        }
    }
    
//    @Override
//    public void onUpdate(EntityTameableDragon dragon) {
//        // start burning when in contact with sunlight
//        if (dragon.worldObj.isDaytime() && dragon.isServer()) {
//            float brightness = dragon.getBrightness(1);
//            float threshold = (brightness - 0.4f) * 2;
//            int bx = MathHelper.floor_double(dragon.posX);
//            int by = MathHelper.floor_double(dragon.posY);
//            int bz = MathHelper.floor_double(dragon.posZ);
//
//            if (brightness > 0.5f && dragon.worldObj.rand.nextFloat() * 30 < threshold && dragon.worldObj.canBlockSeeTheSky(bx, by, bz)) {
//                dragon.setFire(8);
//            }
//        }
//    }

    @Override
    public boolean isHabitatEnvironment(EntityTameableDragon dragon) {
        if (dragon.posY > dragon.worldObj.getHeight() * 0.25) {
            // woah dude, too high!
            return false;
        }

        BlockPos pos = dragon.getPosition();
        
        if (dragon.worldObj.canBlockSeeSky(pos)) {
             // sun is shining!
            return false;
        }
        
        if (dragon.worldObj.getLight(pos) > 4) {
            // too bright!
            return false;
        }

        return true;
    }

    @Override
    public String getLivingSound(EntityTameableDragon dragon) {
        return "mob.skeleton.say";
    }
    
    @Override
    public float getSoundPitch(EntityTameableDragon dragon, String sound) {
        // bony sounds need lower pitches, these are large bones!
        if (sound.equals(getLivingSound(dragon))) {
            return 0.5f;
        }
        
        return super.getSoundPitch(dragon, sound);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    @Override
    public void onUpdate(EntityTameableDragon dragon) {
    }

    @Override
    public void onDeath(EntityTameableDragon dragon) {
    }
}
