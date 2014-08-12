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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Network handler for dragon control messages.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Sharable
public class DragonControlChannelHandler extends MessageToMessageCodec<FMLProxyPacket, DragonControlMessage> {

    private static final Logger L = LogManager.getLogger();
    
    private EnumMap<Side, FMLEmbeddedChannel> channels;

    public DragonControlChannelHandler(String channelName) {
        channels = NetworkRegistry.INSTANCE.newChannel(channelName, new ChannelHandler[]{this});
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DragonControlMessage msg, List<Object> out) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        msg.toBytes(buffer);
        
        FMLProxyPacket proxyPacket = new FMLProxyPacket(buffer, ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
        out.add(proxyPacket);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket proxy, List<Object> out) throws Exception {
        DragonControlMessage msg = new DragonControlMessage();
        
        ByteBuf payload = proxy.payload();
        msg.fromBytes(payload);

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        
        if (side == Side.SERVER) {
            NetHandlerPlayServer netHandler = (NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            handleServerSide(netHandler, msg);
        } else {
            NetHandlerPlayClient netHandler = (NetHandlerPlayClient) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            handleClientSide(netHandler, msg);
        }

        out.add(msg);
    }
    
    protected void handleServerSide(NetHandlerPlayServer netHandler, DragonControlMessage msg) {
        EntityPlayerMP player = netHandler.playerEntity;
        if (player.ridingEntity instanceof EntityTameableDragon) {
            EntityTameableDragon dragon = (EntityTameableDragon) player.ridingEntity;
            dragon.setControlFlags(msg.getFlags());
        }
    }
    
    protected void handleClientSide(NetHandlerPlayClient netHandler, DragonControlMessage msg) {
        // unused for now
        L.warn("Recieved unexpected control message from server!");
    }

    public void sendToServer(DragonControlMessage message) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(message);
    }
}
