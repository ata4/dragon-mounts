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
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMountsConfig {
    
    // config properties
    private boolean eggsInChests = false;
    private boolean debug = false;
    
    public DragonMountsConfig(Configuration config) {
        eggsInChests = config.getBoolean("eggsInChests", "server", eggsInChests, "Spawns dragon eggs in generated chests when enabled");
        debug = config.getBoolean("debug", "client", debug, "Debug mode. Unless you're a developer or are told to activate it, you don't want to set this to true.");
        
        if (config.hasChanged()) {
            config.save();
        }
    }

    public boolean isEggsInChests() {
        return eggsInChests;
    }

    public boolean isDebug() {
        return debug;
    }
}
