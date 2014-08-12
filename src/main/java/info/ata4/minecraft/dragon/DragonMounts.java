/*
 ** 2012 August 13
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import info.ata4.minecraft.dragon.server.ServerProxy;

/**
 * Main control class for Forge.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Mod(
    modid = DragonMounts.ID,
    name = DragonMounts.NAME,
    useMetadata = true
)
public class DragonMounts {
    
    public static final String NAME = "Dragon Mounts";
    public static final String ID = "DragonMounts";
    public static final String AID = ID.toLowerCase();
    
    @SidedProxy(
        serverSide = "info.ata4.minecraft.dragon.server.ServerProxy",
        clientSide = "info.ata4.minecraft.dragon.client.ClientProxy"
    )
    public static ServerProxy proxy;
    
    @Instance(ID)
    public static DragonMounts instance;
    
    public static DragonMountsConfig getConfig() {
        return proxy.getConfig();
    }
    
    private ModMetadata metadata;
    
    public ModMetadata getMetadata() {
        return metadata;
    }
    
    @EventHandler
    public void onPreInit(FMLPreInitializationEvent evt) {
        metadata = evt.getModMetadata();
        proxy.onPreInit(evt);
    }

    @EventHandler
    public void onInit(FMLInitializationEvent evt) {
        proxy.onInit(evt);
    }
    
    @EventHandler
    public void onServerStarted(FMLServerStartedEvent evt) {
        proxy.onServerStarted(evt);
    }
    
    @EventHandler
    public void onServerStopped(FMLServerStoppedEvent evt) {
        proxy.onServerStopped(evt);
    }
}
