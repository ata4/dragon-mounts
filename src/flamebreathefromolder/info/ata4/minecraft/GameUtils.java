/*
** 2011 December 21
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;

/**
 * Small game utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GameUtils {
    
    private static final Minecraft mc = ModLoader.getMinecraftInstance();
    
    private GameUtils() {
    }
    
    public static Minecraft getMinecraft() {
        return mc;
    }

    public static boolean isInCreativeMode() {
        return mc.playerController.isInCreativeMode();
    }
    
    public static boolean consumePlayerEquippedItem(EntityPlayer player, Item... items) {
        ItemStack itemStack = player.getCurrentEquippedItem();
        
        if (itemStack == null) {
            return false;
        }
        
        for (Item item : items) {
            if (itemStack.getItem().shiftedIndex == item.shiftedIndex) {
                // don't reduce stack in creative mode
                if (!isInCreativeMode()) {
                    itemStack.stackSize--;
                    if (itemStack.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                }

                return true;
            }
        }
        
        return false;
    }
}
