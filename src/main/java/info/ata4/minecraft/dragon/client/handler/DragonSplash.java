/*
 ** 2014 January 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.handler;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.util.reflection.PrivateAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

/**
 * Replaces the splash text with a random custom one sometimes.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonSplash implements PrivateAccessor {

	private static final Logger L = LogManager.getLogger();
	private static final ResourceLocation RESOURCE_SPLASHES = new ResourceLocation(DragonMounts.AID, "splashes.txt");

	private final Random rand = new Random();
	private List<String> splashLines;

	public DragonSplash() {
		try {
			InputStream is = null;
			try {
				is = Minecraft.getMinecraft().getResourceManager().getResource(RESOURCE_SPLASHES).getInputStream();
				splashLines = IOUtils.readLines(is, "UTF-8");
			} finally {
				IOUtils.closeQuietly(is);
			}
		} catch (Throwable t) {
			L.warn("Can't load splashes", t);
		}
	}

	@SubscribeEvent
	public void onOpenGui(GuiOpenEvent evt) {
		GuiScreen gui = evt.getGui();
		if (gui instanceof GuiMainMenu) {
			try {
				GuiMainMenu menu = (GuiMainMenu) gui;
				String splash = mainMenuGetSplashText(menu);
				if (splash.equals("Kind of dragon free!")) {
					splash = "Not really dragon free!";
					mainMenuSetSplashText(menu, splash);
				} else if (splashLines != null && !splashLines.isEmpty() && rand.nextInt(10) == 0) {
					splash = splashLines.get(rand.nextInt(splashLines.size()));
					mainMenuSetSplashText(menu, splash);
				}
			} catch (Throwable t) {
				L.warn("Can't override splash", t);
			}
		}
	}
}
