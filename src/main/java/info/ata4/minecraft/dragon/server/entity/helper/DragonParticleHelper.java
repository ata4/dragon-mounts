 /*
 ** 2014 January 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.util.EnumParticleTypes;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonParticleHelper extends DragonHelper {
    
    public DragonParticleHelper(EntityTameableDragon dragon) {
        super(dragon);
    }
    
    public void spawnBodyParticle(EnumParticleTypes type) {
        double ox, oy, oz;
        float s = dragon.getScale() * 1.2f;
        
        switch (type) {
            case EXPLOSION_NORMAL:
                ox = rand.nextGaussian() * s;
                oy = rand.nextGaussian() * s;
                oz = rand.nextGaussian() * s;
                break;
                
            case CLOUD:
                ox = (rand.nextDouble() - 0.5) * 0.1;
                oy = rand.nextDouble() * 0.2;
                oz = (rand.nextDouble() - 0.5) * 0.1;
                break;
                
            case REDSTONE:
                ox = 0.8;
                oy = 0;
                oz = 0.8;
                break;
                
            default:
                ox = 0;
                oy = 0;
                oz = 0;
        }
        
        // use generic random box spawning
        double x = dragon.posX + (rand.nextDouble() - 0.5) * dragon.width * s;
        double y = dragon.posY + (rand.nextDouble() - 0.5) * dragon.height * s;
        double z = dragon.posZ + (rand.nextDouble() - 0.5) * dragon.width * s;

        dragon.worldObj.spawnParticle(type, x, y, z, ox, oy, oz);
    }
    
    public void spawnBodyParticles(EnumParticleTypes type, int baseAmount) {
        int amount = (int) (baseAmount * dragon.getScale());
        for (int i = 0; i < amount; i++) {
            spawnBodyParticle(type);
        }
    }
    
    public void spawnBodyParticles(EnumParticleTypes type) {
        spawnBodyParticles(type, 32);
    }

    @Override
    public void onDeathUpdate() {
        if (dragon.isClient() && !dragon.isEgg() && dragon.deathTime < dragon.getMaxDeathTime() - 20) {
            spawnBodyParticles(EnumParticleTypes.CLOUD, 4);
        }
    }
}
