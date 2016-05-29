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
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedForest extends DragonBreed {
	
    private static final int GRASS_LIGHT_THRESHOLD = 4;
    
    DragonBreedForest() {
        super("forest", 0x2d6e00);
        
        addHabitatBlock(Blocks.log);
        addHabitatBlock(Blocks.log2);
        addHabitatBlock(Blocks.leaves);
        addHabitatBlock(Blocks.leaves2);
        addHabitatBlock(Blocks.yellow_flower);
        addHabitatBlock(Blocks.red_flower);
        addHabitatBlock(Blocks.mossy_cobblestone);
        addHabitatBlock(Blocks.vine);
        addHabitatBlock(Blocks.sapling);
        
        addHabitatBiome(Biomes.forest);
        addHabitatBiome(Biomes.forestHills);
        addHabitatBiome(Biomes.jungle);
        addHabitatBiome(Biomes.jungleHills);
    }
    
    @Override
    protected void placeFootprintBlock(EntityTameableDragon dragon, BlockPos blockPos) {
        World world = dragon.worldObj;
        
        // grow mushrooms and plants
        BlockPos blockPosGround = blockPos.down();
        BlockPos blockPosSurface = blockPos;
        
        IBlockState blockStateUnderFoot = world.getBlockState(blockPosGround);
        Block blockUnderFoot = blockStateUnderFoot.getBlock();
        
        boolean plantFlower = false;
        boolean plantMushroom = false;
        
        if (blockUnderFoot == Blocks.grass) {
            // plant flowers on grass
            plantFlower = true;
        } else if (blockUnderFoot == Blocks.dirt) {
            DirtType dirtType = blockStateUnderFoot.getValue(BlockDirt.VARIANT);
            if (dirtType != DirtType.DIRT) {
                // plant mushrooms on special dirt types
                plantMushroom = true;
            } else if (world.getLightFromNeighbors(blockPosSurface) >= GRASS_LIGHT_THRESHOLD) {
                // turn normal dirt green if there's enough light
                world.setBlockState(blockPosGround, Blocks.grass.getDefaultState());
                // also add flowers randomly
                plantFlower = world.rand.nextBoolean();
            }
        } else if (blockUnderFoot == Blocks.mycelium) {
            // always plant mushrooms on mycelium
            plantMushroom = true;
        }
        
        // pick plant
        BlockBush blockPlant = null;
        IBlockState statePlant = null;
        
        if (plantFlower) {
            EnumFlowerType flower = world.getBiomeGenForCoords(blockPosSurface)
                    .pickRandomFlower(world.rand, blockPosSurface);
            BlockFlower blockFlower = flower.getBlockType().getBlock();
            
            blockPlant = blockFlower;
            statePlant = blockFlower.getDefaultState()
                    .withProperty(blockFlower.getTypeProperty(), flower);
        }
        
        if (plantMushroom) {
            blockPlant = (world.rand.nextBoolean() ?
                    Blocks.red_mushroom : Blocks.brown_mushroom);
            statePlant = blockPlant.getDefaultState();
        }
        
        // place plant if defined        
        if (blockPlant != null && blockPlant.canBlockStay(world, blockPosSurface, statePlant)) {
            world.setBlockState(blockPosSurface, statePlant);
        }
    }
    
    @Override
    protected float getFootprintChance() {
        return 0.05f;
    }

    @Override
    public void onEnable(EntityTameableDragon dragon) {
    }

    @Override
    public void onDisable(EntityTameableDragon dragon) {
    }

    @Override
    public void onDeath(EntityTameableDragon dragon) {
    }
}

