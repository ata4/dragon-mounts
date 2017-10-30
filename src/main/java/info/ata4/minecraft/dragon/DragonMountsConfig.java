/*
 ** 2013 May 30
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon;

import net.minecraftforge.common.config.Configuration;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMountsConfig {

	private final Configuration config;

	// config properties
	private boolean disableBlockOverride = false;
	private boolean debug = false;

	public DragonMountsConfig(Configuration config) {
		debug = config.getBoolean("debug", "client", debug, "Debug mode. Unless you're a developer or are told to activate it, you don't want to set this to true.");
		disableBlockOverride = config.getBoolean("disableBlockOverride", "client", debug, "Disables right-click override on the vanilla dragon egg block. May help to fix issues with other mods.");

		if (config.hasChanged()) {
			config.save();
		}

		this.config = config;
	}

	public Configuration getParent() {
		return config;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isDisableBlockOverride() {
		return disableBlockOverride;
	}
}
