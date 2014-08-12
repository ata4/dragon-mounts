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
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedIce extends DragonBreed {
    
    private static final Block FOOTPRINT = Blocks.snow_layer;
    private static final float FOOTPRINT_CHANCE = 0.2f;

    public DragonBreedIce() {
        super("ice", "ice", 0x6fc3ff);
        
        addImmunity(DamageSource.magic);
        
        addHabitatBlock(Blocks.snow);
        addHabitatBlock(Blocks.snow_layer);
        addHabitatBlock(Blocks.ice);
        
        addHabitatBiome(BiomeGenBase.frozenOcean);
        addHabitatBiome(BiomeGenBase.frozenRiver);
        addHabitatBiome(BiomeGenBase.iceMountains);
        addHabitatBiome(BiomeGenBase.icePlains);
    }

    @Override
    public void onUpdate(EntityTameableDragon dragon) {
        // place some snow footprints where the dragon walks
        if (dragon.isAdult() && !dragon.isFlying()) {
            World world = dragon.worldObj;
            for (int i = 0; i < 4; i++) {
                if (world.rand.nextFloat() < FOOTPRINT_CHANCE) {
                    continue;
                }
                
                int bx = MathHelper.floor_double(dragon.posX + (i % 2 * 2 - 1) * 0.25);
                int by = MathHelper.floor_double(dragon.posY);
                int bz = MathHelper.floor_double(dragon.posZ + (i / 2 % 2 * 2 - 1) * 0.25);

                if (world.getBlock(bx, by, bz) == Blocks.air
                        && world.getBiomeGenForCoords(bx, bz).getFloatTemperature(bx, by, bz) < 0.8f
                        && FOOTPRINT.canPlaceBlockAt(world, bx, by, bz)) {
                    world.setBlock(bx, by, bz, FOOTPRINT);
                }
            }
        }
    }

}
