/*
 ** 2014 January 31
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
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedForest extends DragonBreed {
    
    private static final Block FOOTPRINT = Blocks.grass;
    private static final float FOOTPRINT_CHANCE = 0.2f;
    
    public DragonBreedForest() {
        super("forest", "forest", 0x2d6e00);
        
        addHabitatBlock(Blocks.log);
        addHabitatBlock(Blocks.log2);
        addHabitatBlock(Blocks.leaves);
        addHabitatBlock(Blocks.leaves2);
        addHabitatBlock(Blocks.yellow_flower);
        addHabitatBlock(Blocks.red_flower);
        addHabitatBlock(Blocks.mossy_cobblestone);
        addHabitatBlock(Blocks.vine);
        addHabitatBlock(Blocks.sapling);
        
        addHabitatBiome(BiomeGenBase.forest);
        addHabitatBiome(BiomeGenBase.forestHills);
        addHabitatBiome(BiomeGenBase.jungle);
        addHabitatBiome(BiomeGenBase.jungleHills);
    }
    
    @Override
    public void onUpdate(EntityTameableDragon dragon) {
        // grow grass on dirt blocks as footprints
        if (dragon.isAdult() && !dragon.isFlying()) {
            World world = dragon.worldObj;
            for (int i = 0; i < 4; i++) {
                if (world.rand.nextFloat() < FOOTPRINT_CHANCE) {
                    continue;
                }
                
                int bx = MathHelper.floor_double(dragon.posX + (i % 2 * 2 - 1) * 0.25);
                int by = MathHelper.floor_double(dragon.posY) - 1;
                int bz = MathHelper.floor_double(dragon.posZ + (i / 2 % 2 * 2 - 1) * 0.25);

                if (world.getBlock(bx, by, bz) == Blocks.dirt
                        && world.canBlockSeeTheSky(bx, by, bz)
                        && FOOTPRINT.canPlaceBlockAt(world, bx, by, bz)) {
                    world.setBlock(bx, by, bz, FOOTPRINT);
                }
            }
        }
    }
}
