/*
** 2016 March 10
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.item;

import info.ata4.minecraft.dragon.server.block.BlockDragonBreedEgg;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ItemDragonBreedEgg extends ItemBlock {
    
    public static final ItemDragonBreedEgg INSTANCE = new ItemDragonBreedEgg();
    
    public ItemDragonBreedEgg() {
        super(BlockDragonBreedEgg.INSTANCE);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        EnumDragonBreed type = EnumDragonBreed.META_MAPPING.inverse().get(stack.getMetadata());
        String breedName = I18n.translateToLocal("entity.DragonMounts.DragonMount." + type.getName() + ".name");
        return I18n.translateToLocalFormatted("item.dragonEgg.name", breedName);
    }
}
