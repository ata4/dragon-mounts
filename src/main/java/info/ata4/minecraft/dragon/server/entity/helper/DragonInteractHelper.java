/*
** 2016 April 24
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.interact.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonInteractHelper extends DragonHelper {
    
    private final List<DragonInteract> actions = new ArrayList<>();
    
    public DragonInteractHelper(EntityTameableDragon dragon) {
        super(dragon);
        
        actions.add(new DragonInteractEat(dragon));
        actions.add(new DragonInteractTame(dragon));
        actions.add(new DragonInteractSaddle(dragon));
        actions.add(new DragonInteractSit(dragon));
        actions.add(new DragonInteractRide(dragon));
    }
    
    public boolean interact(EntityPlayer player, ItemStack item) {
        return actions.stream().anyMatch(action -> action.interact(player, item));
    }
    
}
