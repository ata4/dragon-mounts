/*
 ** 2014 March 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import io.netty.channel.ChannelHandler.Sharable;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Network handler for dragon control messages.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Sharable
public class DragonControlMessageHandler implements IMessageHandler<DragonControlMessage, IMessage> {

    private static final Logger L = LogManager.getLogger();

    @Override
    public IMessage onMessage(DragonControlMessage message, MessageContext ctx) {
        // check if the server is messing with the protocol
        if (ctx.side == Side.CLIENT) {
            L.warn("Recieved unexpected control message from server!");
            return null;
        }
        
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        
        if (player.ridingEntity instanceof EntityTameableDragon) {
            EntityTameableDragon dragon = (EntityTameableDragon) player.ridingEntity;
            dragon.setControlFlags(message.getFlags());
        }
        
        // receive only
        return null;
    }
}
