/*
** 2011 December 21
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon.server.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;


/**
 * Small item utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ItemUtils {

    private ItemUtils() {
        // static utility class
    }
    
    /**
     * Consumes the currently equipped item of a player if it matches the item
     * type in the parameters. The stack will be decreased or removed only if
     * the player is not in creative mode.
     * 
     * @param player player to check
     * @param items one or more types of items that should be consumed. Only the
     *              first match will be consumed.
     * @return the consumed item type or null if no matching item was equipped.
     */
    public static Item consumeEquipped(EntityPlayer player, Item... items) {
        ItemStack itemStack = player.getHeldItemMainhand();
        
        if (itemStack == null) {
            return null;
        }
        
        Item equippedItem = itemStack.getItem();
        
        for (Item item : items) {
            if (item == equippedItem) {
                // don't reduce stack in creative mode
                if (!player.capabilities.isCreativeMode) {
                    itemStack.stackSize--;
                }

                // required because the stack isn't reduced in onItemRightClick()
                if (itemStack.stackSize <= 0) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                }

                return item;
            }
        }
        
        return null;
    }
    
    public static boolean consumeEquipped(EntityPlayer player, Item item) {
        return consumeEquipped(player, new Item[]{item}) != null;
    }
    
    /**
     * Checks if a player has food equipped.
     * 
     * @param player player to check
     * @return true if the player has a food item selected
     */
    public static boolean hasEquippedFood(EntityPlayer player) {
        ItemStack itemStack = player.getHeldItemMainhand();
        
        if (itemStack == null) {
            return false;
        }
        
        return itemStack.getItem() instanceof ItemFood;
    }
    
    /**
     * Checks if a player has items equipped that can be used with a right-click.
     * Typically applies for weapons, food and tools.
     * 
     * @param player player to check
     * @return true if the player has an usable item equipped
     */
    public static boolean hasEquippedUsable(EntityPlayer player) {
        ItemStack itemStack = player.getHeldItemMainhand();
        
        if (itemStack == null) {
            return false;
        }
        
        return itemStack.getItemUseAction() != EnumAction.NONE;
    }
    
    /**
     * Checks if a player has a specific item equipped.
     * 
     * @param player player to check
     * @param item required item type
     * @return true if the player has the given item equipped
     */
    public static boolean hasEquipped(EntityPlayer player, Item item) {
        ItemStack itemStack = player.getHeldItemMainhand();
        
        if (itemStack == null) {
            return false;
        }
        
        return itemStack.getItem() == item;
    }
}
