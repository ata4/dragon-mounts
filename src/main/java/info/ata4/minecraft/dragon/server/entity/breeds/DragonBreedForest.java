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
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedForest extends DragonBreed {
	
    //private static final Block FOOTPRINT = Blocks.grass;
    private static final float FOOTPRINT_CHANCE = 0.05f;
    
    DragonBreedForest() {
        super("forest", 0x2d6e00);
        
        addHabitatBlock(Blocks.LOG);
        addHabitatBlock(Blocks.LOG2);
        addHabitatBlock(Blocks.LEAVES);
        addHabitatBlock(Blocks.LEAVES2);
        addHabitatBlock(Blocks.YELLOW_FLOWER);
        addHabitatBlock(Blocks.RED_FLOWER);
        addHabitatBlock(Blocks.MOSSY_COBBLESTONE);
        addHabitatBlock(Blocks.VINE);
        addHabitatBlock(Blocks.SAPLING);
        
        addHabitatBiome(Biomes.FOREST);
        addHabitatBiome(Biomes.FOREST_HILLS);
        addHabitatBiome(Biomes.JUNGLE);
        addHabitatBiome(Biomes.JUNGLE_HILLS);
    }

    @Override
    public void onUpdate(EntityTameableDragon dragon) {
        // grow grass on dirt blocks as footprints
        if (dragon.isAdult() && !dragon.isFlying()) {
            World world = dragon.worldObj;
            for (int i = 0; i < 4; i++) {

                if(world.rand.nextFloat() < FOOTPRINT_CHANCE) {
                int bx = MathHelper.floor_double(dragon.posX + (i % 2 * 2 - 1) * 0.25);
                int by = MathHelper.floor_double(dragon.posY) - 1;
                int bz = MathHelper.floor_double(dragon.posZ + (i / 2 % 2 * 2 - 1) * 0.25);
                BlockPos blockPosUnderFoot = new BlockPos(bx, by, bz);
                BlockPos blockPosOnSurface = blockPosUnderFoot.up();
                IBlockState blockStateUnderFoot = world.getBlockState(blockPosUnderFoot);
                //IBlockState blockStateOnSurface = world.getBlockState(blockPosOnSurface);

                if(world.isAirBlock(blockPosOnSurface)) {
                	
                	Block blockUnderFoot = blockStateUnderFoot.getBlock();
                	//Block blockOnSurface = blockStateOnSurface.getBlock();
                	
                    if(blockUnderFoot == Blocks.GRASS) {

                    	EnumFlowerType flower = world.getBiomeGenForCoords(blockPosOnSurface)
                    			.pickRandomFlower(world.rand, blockPosOnSurface);
                    	BlockFlower blockFlower = flower.getBlockType().getBlock();
                    	IBlockState blockState = blockFlower.getDefaultState()
                    			.withProperty(blockFlower.getTypeProperty(), flower);
                    	if(blockFlower.canBlockStay(world, blockPosOnSurface, blockState)) {
                    		world.setBlockState(blockPosOnSurface, blockState);
                    	}
                    } else if(blockUnderFoot == Blocks.DIRT) {
                    	
                    	DirtType dirtType = blockStateUnderFoot.getValue(BlockDirt.VARIANT);
                    	if(dirtType == DirtType.PODZOL) {
                    		BlockMushroom mushroom = (BlockMushroom) (world.rand.nextBoolean()
                    				? Blocks.RED_MUSHROOM
                    				: Blocks.BROWN_MUSHROOM);
                    		if(mushroom.canBlockStay(world,
                    				blockPosOnSurface, mushroom.getDefaultState())) {
                    			world.setBlockState(blockPosOnSurface, mushroom.getDefaultState());
                    			}
                    		}
                    	}
                	}
                }
            }
        }
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

