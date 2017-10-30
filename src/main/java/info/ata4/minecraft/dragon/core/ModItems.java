package info.ata4.minecraft.dragon.core;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.item.ItemDragonBreedEgg;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Joseph on 10/30/2017.
 */
public class ModItems {
	@GameRegistry.ObjectHolder(DragonMounts.ID + ":dragon_egg")
	public static Item dragon_egg = new ItemDragonBreedEgg();
}
