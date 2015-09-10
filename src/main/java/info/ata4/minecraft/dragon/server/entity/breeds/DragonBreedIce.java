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
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
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

          double bx = dragon.posX + (i % 2 * 2 - 1) * 0.25;
          double by = dragon.posY + 0.5;
          double bz = dragon.posZ + (i / 2 % 2 * 2 - 1) * 0.25;
          BlockPos blockPos = new BlockPos(bx, by, bz);
// from EntitySnowman.onLivingUpdate, with slight tweaks
          if (world.getBlockState(new BlockPos(bx, by, bz)).getBlock().getMaterial() == Material.air
                  && world.getBiomeGenForCoords(new BlockPos(bx, 0, bz)).getFloatTemperature(blockPos) <= 0.8F
                  && FOOTPRINT.canPlaceBlockAt(world, blockPos)) {
            world.setBlockState(blockPos, FOOTPRINT.getDefaultState());
          }
        }
      }
    }
}
