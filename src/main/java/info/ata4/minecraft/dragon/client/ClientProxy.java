/*
 ** 2012 August 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.gui.GuiDragonDebug;
import info.ata4.minecraft.dragon.client.handler.DragonControl;
import info.ata4.minecraft.dragon.client.handler.DragonEntityWatcher;
import info.ata4.minecraft.dragon.client.handler.DragonSplash;
import info.ata4.minecraft.dragon.client.render.DragonRenderer;
import info.ata4.minecraft.dragon.server.ServerProxy;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraftforge.common.MinecraftForge;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClientProxy extends ServerProxy {
    
    @Override
    public void onInit(FMLInitializationEvent evt) {
        super.onInit(evt);
        
        RenderingRegistry.registerEntityRenderingHandler(EntityTameableDragon.class, new DragonRenderer());

        FMLCommonHandler.instance().bus().register(new DragonEntityWatcher());
        FMLCommonHandler.instance().bus().register(new DragonControl(getNetwork()));
        
        MinecraftForge.EVENT_BUS.register(new DragonSplash());
        
        if (DragonMounts.instance.getConfig().isDebug()) {
            MinecraftForge.EVENT_BUS.register(new GuiDragonDebug());
        }
    }
}
