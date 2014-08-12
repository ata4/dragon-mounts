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

import info.ata4.minecraft.dragon.util.BasicModConfig;
import java.io.File;
import net.minecraftforge.common.config.Property;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMountsConfig extends BasicModConfig {
    
    // non-string defaults so I don't have to hardcode them twice...
    private static final boolean DEF_EGGS_IN_CHESTS = false;
    private static final int DEF_DRAGON_ENTITY_ID = -1;
    private static final boolean DEF_DEBUG = false;
    
    // config properties
    private Property eggsInChests;
    private Property dragonEntityID;
    private Property debug;
    
    public DragonMountsConfig(File configFile) {
        super(configFile);
    }
    
    @Override
    protected void init() {
        eggsInChests = config.get("server", "eggsInChests", DEF_EGGS_IN_CHESTS, "Spawns dragon eggs in generated chests when enabled");
        dragonEntityID = config.get("server", "dragonEntityID", DEF_DRAGON_ENTITY_ID, "Overrides the entity ID for dragons to fix problems with manual IDs from other mods.\nSet to -1 for automatic assignment (recommended).\nWarning: wrong values may cause crashes and loss of data!");
        debug = config.get("client", "debug", DEF_EGGS_IN_CHESTS, "Debug mode. Unless you're a developer or are told to activate it, you don't want to set this to true.");
    }
    
    public boolean isEggsInChests() {
        return eggsInChests.getBoolean(DEF_EGGS_IN_CHESTS);
    }
    
    public void setEggsInChests(boolean enabled) {
        eggsInChests.set(enabled);
    }
    
    public int getDragonEntityID() {
        return dragonEntityID.getInt(DEF_DRAGON_ENTITY_ID);
    }
    
    public void getDragonEntityID(int id) {
        dragonEntityID.set(id);
    }
    
    public boolean isDebug() {
        return debug.getBoolean(DEF_DEBUG);
    }
    
    public void setDebug(boolean enabled) {
        debug.set(enabled);
    }
}
