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
import net.minecraft.item.ItemStack;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonInteractRide extends DragonInteract {

	public DragonInteractRide(EntityTameableDragon dragon) {
		super(dragon);
	}

	@Override
	public boolean interact(EntityPlayer player, ItemStack item) {
		if (dragon.isServer() && dragon.isTamedFor(player) &&
				dragon.isSaddled() && !ItemUtils.hasEquippedUsable(player)) {
			dragon.setRidingPlayer(player);
			return true;
		}

		return false;
	}
}
