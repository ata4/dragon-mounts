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

import info.ata4.minecraft.dragon.server.CommonProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

/**
 * Main control class for Forge.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Mod(
		modid = DragonMounts.ID,
		name = DragonMounts.NAME,
		version = DragonMounts.VERSION,
		useMetadata = true,
		guiFactory = "info.ata4.minecraft.dragon.DragonMountsConfigGuiFactory"
)
public class DragonMounts {

	public static final String NAME = "Dragon Mounts";
	public static final String ID = "dragonmounts";
	public static final String AID = ID.toLowerCase();
	public static final String VERSION = "@VERSION@";

	@SidedProxy(
			serverSide = "info.ata4.minecraft.dragon.server.CommonProxy",
			clientSide = "info.ata4.minecraft.dragon.client.ClientProxy"
	)
	public static CommonProxy proxy;

	@Instance(ID)
	public static DragonMounts instance;

	private ModMetadata metadata;
	private DragonMountsConfig config;

	public DragonMountsConfig getConfig() {
		return config;
	}

	public ModMetadata getMetadata() {
		return metadata;
	}

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent evt) {
		config = new DragonMountsConfig(new Configuration(evt.getSuggestedConfigurationFile()));
		metadata = evt.getModMetadata();
		proxy.onPreInit(evt);
	}

	@EventHandler
	public void onInit(FMLInitializationEvent evt) {
		proxy.onInit(evt);
	}

	@EventHandler
	public void onPostInit(FMLPostInitializationEvent event) {
		proxy.onPostInit(event);
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent evt) {
		proxy.onServerStarting(evt);
	}

	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent evt) {
		proxy.onServerStopped(evt);
	}
}
