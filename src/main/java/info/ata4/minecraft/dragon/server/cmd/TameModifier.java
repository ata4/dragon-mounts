/*
 ** 2015 Juni 30
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.cmd;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class TameModifier implements EntityModifier {
    
    private final EntityPlayerMP player;
    
    TameModifier(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void modify(EntityTameableDragon dragon) {
        dragon.tamedFor(player, true);
    }
}
