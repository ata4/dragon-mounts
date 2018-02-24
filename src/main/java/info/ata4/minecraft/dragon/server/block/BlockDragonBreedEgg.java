/*
** 2016 March 09
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.block;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BlockDragonBreedEgg extends BlockDragonEgg {

	public static final PropertyEnum<EnumDragonBreed> BREED = PropertyEnum.create("breed", EnumDragonBreed.class);
	public static BlockDragonBreedEgg DRAGON_BREED_EGG;
	public static final BlockDragonBreedEgg[] BLOCK_EGG = {DRAGON_BREED_EGG = new BlockDragonBreedEgg()};
	public int meta;
	private EntityTameableDragon dragon;

	public BlockDragonBreedEgg() {
		setUnlocalizedName("dragonEgg");
		setHardness(0);
		setResistance(30);
		setSoundType(SoundType.WOOD);
		setLightLevel(0.125f);
		setCreativeTab(CreativeTabs.MISC);

	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{BREED});
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		this.meta = meta;
		return getDefaultState().withProperty(BREED, EnumDragonBreed.META_MAPPING.inverse().get(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumDragonBreed type = (EnumDragonBreed) state.getValue(BREED);
		return EnumDragonBreed.META_MAPPING.get(type);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		EnumDragonBreed.META_MAPPING.values().forEach(index -> items.add(new ItemStack(this, 1, index)));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		this.checkFall(worldIn, pos);
	}

	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
		return;
	}

	/**
	 * Called when the block is right clicked by a player.
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return true;
	}

	private void checkFall(World worldIn, BlockPos pos) {
		if (worldIn.isAirBlock(pos.down()) && BlockFalling.canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0) {
			int i = 32;

			if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				worldIn.spawnEntity(new EntityFallingBlock(worldIn, (double) ((float) pos.getX() + 0.5F), (double) pos.getY(), (double) ((float) pos.getZ() + 0.5F), this.getStateFromMeta(meta)));
			} else {
				worldIn.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos; worldIn.isAirBlock(blockpos) && BlockFalling.canFallThrough(worldIn.getBlockState(blockpos)) && blockpos.getY() > 0; blockpos = blockpos.down()) {
					;
				}

				if (blockpos.getY() > 0) {
					worldIn.setBlockState(blockpos, this.getStateFromMeta(meta), 2);
				}
			}
		}
	}
}
