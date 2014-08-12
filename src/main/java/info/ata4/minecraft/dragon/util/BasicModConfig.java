/*
 ** 2014 January 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.util;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract helper class for easier handling with Forge's strange config classes.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class BasicModConfig {
    
    private static final Logger L = LogManager.getLogger();
    
    protected Configuration config;
    private boolean failsafe = false;
    
    public BasicModConfig(File configFile) {
        try {
            config = new Configuration(configFile);
        } catch (Throwable t) {
            config = new Configuration();
            failsafe = true;
            L.warn("Error in configuration file, using defaults", t);
        }
        
        init();
        save();
    }
    
    public void reload() {
        if (failsafe) {
            config = new Configuration();
            init();
        } else {
            try {
                config.load();
            } catch (Throwable t) {
                L.warn("Error in configuration file", t);
            }
        }
    }
    
    public void save() {
        if (!failsafe && config.hasChanged()) {
            config.save();
        }
    }
    
    protected abstract void init();
}
