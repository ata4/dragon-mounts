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
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBreedForest extends DragonBreed {
	
    //private static final Block FOOTPRINT = Blocks.grass;
    private static final float FOOTPRINT_CHANCE = 0.05f;
    
    DragonBreedForest() {
        super(EnumDragonBreed.FOREST, "forest", 0x2d6e00);
        
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
                	
                    if(blockUnderFoot == Blocks.grass) {

                    	EnumFlowerType flower = world.getBiomeGenForCoords(blockPosOnSurface)
                    			.pickRandomFlower(world.rand, blockPosOnSurface);
                    	BlockFlower blockFlower = flower.getBlockType().getBlock();
                    	IBlockState blockState = blockFlower.getDefaultState()
                    			.withProperty(blockFlower.getTypeProperty(), flower);
                    	if(blockFlower.canBlockStay(world, blockPosOnSurface, blockState)) {
                    		world.setBlockState(blockPosOnSurface, blockState);
                    	}
                    } else if(blockUnderFoot == Blocks.dirt) {
                    	
                    	DirtType dirtType = blockStateUnderFoot.getValue(BlockDirt.VARIANT);
                    	if(dirtType == DirtType.PODZOL) {
                    		BlockMushroom mushroom = (BlockMushroom) (world.rand.nextBoolean()
                    				? Blocks.red_mushroom
                    				: Blocks.brown_mushroom);
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

