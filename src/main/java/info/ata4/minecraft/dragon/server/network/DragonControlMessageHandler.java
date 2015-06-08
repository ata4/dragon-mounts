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

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import io.netty.channel.ChannelHandler.Sharable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
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
    public IMessage onMessage(final DragonControlMessage message, MessageContext ctx) {
        if (ctx.side != Side.SERVER) {
            L.warn("DragonControlMessage received on wrong side:" + ctx.side);
            return null;
        }

        // we know for sure that this handler is only used on the server side, so it is ok to assume
        //  that the ctx handler is a serverhandler, and that WorldServer exists.
        // Packets received on the client side must be handled differently!  See MessageHandlerOnClient

        final EntityPlayerMP sendingPlayer = ctx.getServerHandler().playerEntity;
        if (sendingPlayer == null) {
            L.warn("EntityPlayerMP was null when DragonControlMessage was received");
            return null;
        }

        // This code creates a new task which will be executed by the server during the next tick,
        //  for example see MinecraftServer.updateTimeLightAndEntities(), just under section
        //      this.theProfiler.startSection("jobs");
        //  In this case, the task is to call messageHandlerOnServer.processMessage(message, sendingPlayer)
        final WorldServer playerWorldServer = sendingPlayer.getServerForPlayer();
        playerWorldServer.addScheduledTask(new Runnable() {
            public void run() {
                processMessage(message, sendingPlayer);
            }
        });

        return null;
    }

    // This message is called from the Server thread.
    void processMessage(DragonControlMessage message, EntityPlayerMP sendingPlayer)
    {
        if (sendingPlayer.ridingEntity instanceof EntityTameableDragon) {
            EntityTameableDragon dragon = (EntityTameableDragon)sendingPlayer.ridingEntity;
            dragon.setControlFlags(message.getFlags());
        }
    }
}
