package info.ata4.minecraft.dragon.server.block;

import info.ata4.minecraft.dragon.DragonMounts;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Joseph on 10/30/2017.
 */
public class ModBlocks {

	@GameRegistry.ObjectHolder(DragonMounts.ID + ":dragon_egg")
	public static Block dragon_egg = new BlockDragonBreedEgg();

}
