/*
** 2016 April 24
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.interact;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonInteractEat extends DragonInteract {

    public DragonInteractEat(EntityTameableDragon dragon) {
        super(dragon);
    }

    @Override
    public boolean interact(EntityPlayer player, ItemStack item) {
        // eat only if hurt
        if (dragon.isServer() && dragon.getHealthRelative() < 1) {
            ItemFood food = (ItemFood) ItemUtils.consumeEquipped(player,
                    dragon.getBreed().getFoodItems());

            // heal only if the food was actually consumed
            if (food != null) {
                dragon.heal(food.getHealAmount(item));
                dragon.playSound(dragon.getSoundManager().getEatSound(), 0.7f, 1);
                return true;
            }
        }

        return false;
    }
    
}
