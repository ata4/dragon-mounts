/*
 ** 2013 October 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.breeds.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central dragon breed registry.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedRegistry {
    
    private static DragonBreedRegistry instance;
    
    public static DragonBreedRegistry getInstance() {
        if (instance == null) {
            instance = new DragonBreedRegistry();
        }
        
        return instance;
    }
    
    private Map<String, DragonBreed> breeds = new HashMap<String, DragonBreed>();
    
    private DragonBreedRegistry() {
        add(new DragonBreedAir());
        add(new DragonBreedEnd());
        add(new DragonBreedFire());
        add(new DragonBreedIce());
        add(new DragonBreedGhost());
        add(new DragonBreedWater());
        add(new DragonBreedNether());
        add(new DragonBreedForest());
    }
    
    private void add(DragonBreed breed) {
        breeds.put(breed.getName(), breed);
    }
    
    public List<DragonBreed> getBreeds() {
        return new ArrayList<DragonBreed>(breeds.values());
    }
    
    public DragonBreed getBreedByName(String name) {
        return breeds.get(name);
    }
}
