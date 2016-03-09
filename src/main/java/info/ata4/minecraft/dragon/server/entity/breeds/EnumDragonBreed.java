/*
** 2016 March 08
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.breeds;

import java.lang.reflect.InvocationTargetException;
import net.minecraft.util.IStringSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum EnumDragonBreed implements IStringSerializable {
    
    AIR(DragonBreedAir.class),
    END(DragonBreedEnd.class),
    FIRE(DragonBreedFire.class),
    FOREST(DragonBreedForest.class),
    GHOST(DragonBreedGhost.class),
    ICE(DragonBreedIce.class),
    NETHER(DragonBreedNether.class),
    WATER(DragonBreedWater.class);
    
    private static final Logger L = LogManager.getLogger();
    public static EnumDragonBreed DEFAULT = END;
    
    private final DragonBreed breed;
    
    private EnumDragonBreed(Class<? extends DragonBreed> factory) {
        try {
            this.breed = factory.getDeclaredConstructor(EnumDragonBreed.class).newInstance(this);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalArgumentException ex) {
            throw new RuntimeException("Incompatible breed factory " + factory, ex);
        } catch (SecurityException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public DragonBreed getBreed() {
        return breed;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }
    
}
