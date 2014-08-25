/*
 ** 2013 October 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.handler;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import info.ata4.minecraft.dragon.server.network.DragonControlMessage;
import java.util.BitSet;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * Client side event handler for dragon control messages.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonControl {
    
    public static final String KEY_CATEGORY = "key.categories.dragonmount";
    public static final KeyBinding KEY_FLY_UP = new KeyBinding("key.dragon.flyUp", Keyboard.KEY_R, KEY_CATEGORY);
    public static final KeyBinding KEY_FLY_DOWN = new KeyBinding("key.dragon.flyDown", Keyboard.KEY_F, KEY_CATEGORY);

    private final DragonControlMessage dcm = new DragonControlMessage();
    private final SimpleNetworkWrapper network;
    
    public DragonControl(SimpleNetworkWrapper network) {
        this.network = network;
        
        ClientRegistry.registerKeyBinding(KEY_FLY_UP);
        ClientRegistry.registerKeyBinding(KEY_FLY_DOWN);
    }
    
    @SubscribeEvent
    public void onTick(ClientTickEvent evt) {
        BitSet flags = dcm.getFlags();
        flags.set(0, KEY_FLY_UP.getIsKeyPressed());
        flags.set(1, KEY_FLY_DOWN.getIsKeyPressed());
        
        // send message to server if it has changed
        if (dcm.hasChanged()) {
            network.sendToServer(dcm);
        }
    }
}
