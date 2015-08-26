package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by TGG on 5/08/2015.
 *
 * Models a breathweapon for the dragon
 * Currently does fire only
 */
public class BreathWeapon
{
  public BreathWeapon(EntityTameableDragon i_dragon)
  {
    dragon = i_dragon;
  }

  /** if the hitDensity is high enough, manipulate the block (eg set fire to it)
   * @param world
   * @param blockPosition  the world [x,y,z] of the block
   * @param currentHitDensity
   * @return the updated block hit density
   */
  public BreathAffectedBlock affectBlock(World world, Vec3i blockPosition,
                                                     BreathAffectedBlock currentHitDensity)
  {
    checkNotNull(world);
    checkNotNull(blockPosition);
    checkNotNull(currentHitDensity);

    BlockPos blockPos = new BlockPos(blockPosition);
    IBlockState iBlockState = world.getBlockState(blockPos);
    Block block = iBlockState.getBlock();

    Random rand = new Random();

    // Flammable blocks: set fire to them once they have been exposed enough.  After sufficient exposure, destroy the
    //   block (otherwise -if it's raining, the burning block will keep going out)
    // Non-flammable blocks:
    // 1) liquids (except lava) evaporate
    // 2) If the block can be smelted (eg sand), then convert the block to the smelted version
    // 3) If the block can't be smelted then convert to lava

    for (EnumFacing facing : EnumFacing.values()) {
      BlockPos sideToIgnite = blockPos.offset(facing);
      if (block.isFlammable(world, sideToIgnite, facing)) {
        int flammability = block.getFlammability(world, sideToIgnite, facing);
        float thresholdForIgnition = convertFlammabilityToHitDensityThreshold(flammability);
        float thresholdForDestruction = thresholdForIgnition * 10;
//        System.out.println("Threshold: " + thresholdHitDensity                              //todo remove
//                + ", current:" + facing + "=" + currentHitDensity.getHitDensity(facing));
        float densityOfThisFace = currentHitDensity.getHitDensity(facing);
        if (densityOfThisFace >= thresholdForIgnition && world.isAirBlock(sideToIgnite)) {
          final float MIN_PITCH = 0.8F;
          final float MAX_PITCH = 1.2F;
          final float VOLUME = 1.0F;
          world.playSoundEffect(sideToIgnite.getX() + 0.5, sideToIgnite.getY() + 0.5, sideToIgnite.getZ() + 0.5,
                  "fire.ignite", VOLUME, MIN_PITCH + rand.nextFloat() * (MAX_PITCH - MIN_PITCH));
          world.setBlockState(sideToIgnite, Blocks.fire.getDefaultState());
        }
        if (densityOfThisFace >= thresholdForDestruction) {
          world.setBlockToAir(blockPos);
        }
      }
    }

    BlockBurnProperties burnProperties = getBurnProperties(iBlockState);
    if (burnProperties.burnResult == null
        || currentHitDensity.getMaxHitDensity() < burnProperties.threshold) {
      return currentHitDensity;
    }
    world.setBlockState(blockPos, burnProperties.burnResult);
    return new BreathAffectedBlock();  // reset to zero
  }

  private BlockBurnProperties getBurnProperties(IBlockState iBlockState)
  {
    Block block = iBlockState.getBlock();
    if (blockBurnPropertiesCache.containsKey(block)) {
      return  blockBurnPropertiesCache.get(block);
    }

    BlockBurnProperties blockBurnProperties = new BlockBurnProperties();
    IBlockState result = getSmeltingResult(iBlockState);
    blockBurnProperties.threshold = 20;
    if (result == null) {
      blockBurnProperties.threshold = 3;
      result = getScorchedResult(iBlockState);
    }
    if (result == null) {
      blockBurnProperties.threshold = 5;
      result = getVaporisedLiquidResult(iBlockState);
    }
    if (result == null) {
      blockBurnProperties.threshold = 100;
      result = getMoltenLavaResult(iBlockState);
    }
    blockBurnProperties.burnResult = result;
    blockBurnPropertiesCache.put(block, blockBurnProperties);
    return blockBurnProperties;
  }

  private static class BlockBurnProperties {
    public IBlockState burnResult = null;  // null if no effect
    public float threshold;
  }

  /** if sourceBlock can be smelted, return the smelting result as a block
   * @param sourceBlock
   * @return the smelting result, or null if none
   */
  private static IBlockState getSmeltingResult(IBlockState sourceBlock)
  {
    Block block = sourceBlock.getBlock();
    Item itemFromBlock = Item.getItemFromBlock(block);
    ItemStack itemStack;
    if (itemFromBlock != null && itemFromBlock.getHasSubtypes())     {
      int metadata = block.getMetaFromState(sourceBlock);
      itemStack = new ItemStack(itemFromBlock, 1, metadata);
    } else {
      itemStack = new ItemStack(itemFromBlock);
    }

    ItemStack smeltingResult = FurnaceRecipes.instance().getSmeltingResult(itemStack);
    if (smeltingResult != null) {
      Block smeltedResultBlock = Block.getBlockFromItem(smeltingResult.getItem());
      if (smeltedResultBlock != null) {
        IBlockState iBlockStateSmelted = smeltedResultBlock.getStateFromMeta(smeltingResult.getMetadata());
        return iBlockStateSmelted;
      }
    }
    if (block == Blocks.iron_ore) return Blocks.iron_block.getDefaultState();
    if (block == Blocks.gold_ore) return Blocks.gold_block.getDefaultState();
    if (block == Blocks.emerald_ore) return Blocks.emerald_block.getDefaultState();
    if (block == Blocks.diamond_ore) return Blocks.diamond_block.getDefaultState();
    if (block == Blocks.coal_ore) return Blocks.coal_block.getDefaultState();
    if (block == Blocks.redstone_ore) return Blocks.redstone_block.getDefaultState();
    if (block == Blocks.lapis_ore) return Blocks.lapis_block.getDefaultState();
    if (block == Blocks.quartz_ore) return Blocks.quartz_block.getDefaultState();
    return null;
  }

  /** if sourceBlock is a liquid or snow that can be molten or vaporised, return the result as a block
   *
   * @param sourceBlock
   * @return the vaporised result, or null if none
   */
  private static IBlockState getVaporisedLiquidResult(IBlockState sourceBlock)
  {
    Block block = sourceBlock.getBlock();
    Material material = block.getMaterial();

    if (material == Material.water) {
      return Blocks.air.getDefaultState();
    } else if (material == Material.snow || material == Material.ice) {
      final int SMALL_LIQUID_AMOUNT = 4;
      return Blocks.flowing_water.getDefaultState().withProperty(BlockLiquid.LEVEL, SMALL_LIQUID_AMOUNT);
    } else if (material == Material.packedIce || material == Material.craftedSnow) {
      final int LARGE_LIQUID_AMOUNT = 1;
      return Blocks.flowing_water.getDefaultState().withProperty(BlockLiquid.LEVEL, LARGE_LIQUID_AMOUNT);
    }
    return null;
  }

  /** if sourceBlock is a block that can be melted to lave, return the result as a block
   * @param sourceBlock
   * @return the molten lava result, or null if none
   */
  private static IBlockState getMoltenLavaResult(IBlockState sourceBlock)
  {
    Block block = sourceBlock.getBlock();
    Material material = block.getMaterial();

    if (material == Material.sand || material == Material.clay
            || material == Material.glass || material == Material.iron
            || material == Material.ground || material == Material.rock) {
      final int LARGE_LIQUID_AMOUNT = 1;
      return Blocks.lava.getDefaultState().withProperty(BlockLiquid.LEVEL, LARGE_LIQUID_AMOUNT);
    }
    return null;
  }

  /** if sourceBlock is a block that isn't flammable but can be scorched / changed, return the result as a block
   * @param sourceBlock
   * @return the scorched result, or null if none
   */
  private static IBlockState getScorchedResult(IBlockState sourceBlock)
  {
    Block block = sourceBlock.getBlock();
    Material material = block.getMaterial();

    if (material == Material.grass) {
      return Blocks.dirt.getDefaultState();
    }
    return null;
  }


  private HashMap<Block, BlockBurnProperties> blockBurnPropertiesCache = new HashMap<Block, BlockBurnProperties>();

  /** if the hitDensity is high enough, manipulate the entity (eg set fire to it, damage it)
   * A dragon can't be damaged by its own breathweapon.
   * @param world
   * @param entityID  the ID of the affected entity
   * @param currentHitDensity the hit density
   * @return the updated hit density; null if entity dead, doesn't exist, or otherwise not affected
   */
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity)
  {
    checkNotNull(world);
    checkNotNull(entityID);
    checkNotNull(currentHitDensity);

    if (entityID == dragon.getEntityId()) return null;

      Entity entity = world.getEntityByID(entityID);
    if (entity == null || !(entity instanceof EntityLivingBase) || entity.isDead) {
      return null;
    }

//    System.out.println("Burn " + entity + "=" + entity.getName() + ":" + currentHitDensity.getHitDensity()); //todo remove

    if (entity.isImmuneToFire()) return currentHitDensity;

    final float CATCH_FIRE_THRESHOLD = 2.5F;
    final float BURN_SECONDS_PER_HIT_DENSITY = 1.0F;
    final float DAMAGE_PER_HIT_DENSITY = 0.5F;

    float hitDensity = currentHitDensity.getHitDensity();
    if (hitDensity > CATCH_FIRE_THRESHOLD) {
      entity.setFire((int)(hitDensity * BURN_SECONDS_PER_HIT_DENSITY));
    }
    if (currentHitDensity.applyDamageThisTick()) {
      entity.attackEntityFrom(DamageSource.inFire, hitDensity * DAMAGE_PER_HIT_DENSITY);
    }

    return currentHitDensity;
  }

  /**
   * returns the hitDensity threshold for the given block flammability (0 - 300 as per Block.getFlammability)
   * @param flammability
   * @return the hit density threshold above which the block catches fire
   */
  private float convertFlammabilityToHitDensityThreshold(int flammability)
  {
    checkArgument(flammability >= 0 && flammability <= 300);
    if (flammability == 0) return Float.MAX_VALUE;
    // typical values for items are 5 (coal, logs), 20 (gates etc), 60 - 100 for leaves & flowers & grass
    // want: leaves & flowers to burn instantly; gates to take ~1 second at full power, coal / logs to take ~3 seconds
    // hitDensity of 1 is approximately 1-2 ticks of full exposure from a single beam, so 3 seconds is ~30

    float threshold = 50.0F / flammability;
    return threshold;
  }

  protected EntityTameableDragon dragon;
}
