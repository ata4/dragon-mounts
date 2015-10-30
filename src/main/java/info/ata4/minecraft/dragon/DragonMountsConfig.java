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
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * Holds the configuration information and synchronises the various copies of it
 * The configuration information is stored in three places: 1) in the
 * configuration file on disk, as text 2) in the Configuration object config
 * (accessed by the mod GUI), as text 3) in the MBEConfiguration variables
 * (fields), as native values (integer, double, etc) Usage: Setup: (1) During
 * preInit(), create an instance of DragonMountsConfig a) set up the format of
 * the configuration file b) load the settings from the existing file, or if it
 * doesn't exist yet - create it with default values (2) On the client proxy
 * (not dedicated server), call clientInit() to register an OnConfigChangedEvent
 * handler- your GUI will modify the config object, and when it is closed it
 * will trigger a OnConfigChangedEvent, which should call syncFromGUI(). Usage:
 * (3) You can read the fields such as eggsInChests using the getter methods (4)
 * If you modify the configuration fields, you can save them to disk using
 * syncFromFields() (5) To reload the values from disk, call syncFromFile() (6)
 * If you have used a GUI to alter the config values, call syncFromGUI(). (If
 * you called clientInit(), this will happen automatically) See
 * ForgeModContainer for more examples
 */
public class DragonMountsConfig {
    
    public static final String CATEGORY_NAME_OPTIONS = "category_options";
    public static final String CATEGORY_NAME_DEBUG = "category_debug";

    // config properties
    private boolean eggsInChests = false;
    private int dragonEntityID = -1;
    private boolean debug = false;
    private boolean orbTargetAutoLock = true;
    private boolean orbHighlightTarget = true;
    private boolean orbHolderImmune = true;

    public DragonMountsConfig(Configuration i_config) {
        config = i_config;
        syncFromFile();
    }

    public void clientInit() {
        //register the save config handler to the forge mod loader event bus
        // creates an instance of the static class ConfigEventHandler and has it listen
        // on the FML bus (see Notes and ConfigEventHandler for more information)
        FMLCommonHandler.instance().bus().register(new ConfigEventHandler());
    }

    public boolean isEggsInChests() {
        return eggsInChests;
    }

    public int getDragonEntityID() {
        return dragonEntityID;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isOrbTargetAutoLock() {
        return orbTargetAutoLock;
    }

    public boolean isOrbHighlightTarget() {
        return orbHighlightTarget;
    }

    public boolean isOrbHolderImmune() {
        return orbHolderImmune;
    }

    public Configuration getConfig() {
        return config;
    }

    /**
     * load the configuration values from the configuration file
     */
    public void syncFromFile() {
        syncConfig(true, true);
    }

    /**
     * save the GUI-altered values to disk
     */
    public void syncFromGUI() {
        syncConfig(false, true);
    }

    /**
     * save the member variables (fields) to disk
     */
    public void syncFromFields() {
        syncConfig(false, false);
    }

    /**
     * Synchronise the three copies of the data 1) loadConfigFromFile &&
     * readFieldsFromConfig -> initialise everything from the disk file 2)
     * !loadConfigFromFile && readFieldsFromConfig --> copy everything from the
     * config file (altered by GUI) 3) !loadConfigFromFile &&
     * !readFieldsFromConfig --> copy everything from the native fields
     *
     * @param loadConfigFromFile if true, load the config field from the
     * configuration file on disk
     * @param readFieldsFromConfig if true, reload the member variables from the
     * config field
     */
    private void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig) {
        // ---- step 1 - load raw values from config file (if loadFromFile true) -------------------

        /*Check if this configuration object is the main config file or a child configuration
         *For simple configuration setups, this only matters if you enable global configuration
         *	for your configuration object by using config.enableGlobalConfiguration(),
         *	this will cause your config file to be 'global.cfg' in the default configuration directory
         *  and use it to read/write your configuration options
         */
        if (loadConfigFromFile) {
            config.load();
        }

        /* Using language keys are a good idea if you are using a config GUI
         * This allows you to provide "pretty" names for the config properties
         * 	in a .lang file as well as allow others to provide other localizations
         *  for your mod
         * The language key is also used to get the tooltip for your property,
         * 	the language key for each properties tooltip is langKey + ".tooltip"
         *  If no tooltip lang key is specified in a .lang file, it will default to
         *  the property's comment field
         * prop.setRequiresWorldRestart(true); and prop.setRequiresMcRestart(true);
         *  can be used to tell Forge if that specific property requires a world
         *  or complete Minecraft restart, respectively
         *  Note: if a property requires a world restart it cannot be edited in the
         *   in-world mod settings (which hasn't been implemented yet by Forge), only
         *   when a world isn't loaded
         *   -See the function definitions for more info
         */
        // ---- step 2 - define the properties in the configuration file -------------------
        // The following code is used to define the properties in the configuration file-
        //   their name, type, default / min / max values, a comment.  These affect what is displayed on the GUI.
        // If the file already exists, the property values will already have been read from the file, otherwise they
        //  will be assigned the default value.
        final boolean DEFAULT_EGGS_IN_CHEST = true;
        Property propEggsInChests = config.get(CATEGORY_NAME_OPTIONS, "eggsInChests", DEFAULT_EGGS_IN_CHEST);
        propEggsInChests.comment = "Spawns dragon eggs in generated chests when enabled";
        propEggsInChests.setLanguageKey("gui.config.options.eggs_in_chest");

        final boolean DEFAULT_AUTOLOCK = true;
        Property propOrbTargetAutoLock = config.get(CATEGORY_NAME_OPTIONS, "orbTargetAutoLock", DEFAULT_AUTOLOCK);
        propOrbTargetAutoLock.comment = "Clicking the dragon orb locks on to the target until released";
        propOrbTargetAutoLock.setLanguageKey("gui.config.options.orb_target_auto_lock");

        final boolean DEFAULT_HIGHLIGHT = true;
        Property propOrbHighlightTarget = config.get(CATEGORY_NAME_OPTIONS, "orbHighlightTarget", DEFAULT_HIGHLIGHT);
        propOrbHighlightTarget.comment = "Should the dragon orb show a highlight around the target?";
        propOrbHighlightTarget.setLanguageKey("gui.config.options.orb_highlight_target");

        final boolean DEFAULT_IMMUNE = true;
        Property propOrbHolderImmune = config.get(CATEGORY_NAME_OPTIONS, "orbHolderImmune", DEFAULT_IMMUNE);
        propOrbHolderImmune.comment = "Is the orb holder immune to dragon breath?";
        propOrbHolderImmune.setLanguageKey("gui.config.options.orb_holder_immune");

        final int DEFAULT_ENTITY_ID_AUTOASSIGN = -1;
        final int MIN_ENTITY_ID = -1;
        final int MAX_ENTITY_ID = 255;
        String ENTITY_ID_COMMENT = "Overrides the entity ID for dragons to fix problems with manual IDs from "
                + "other mods.\nSet to -1 for automatic assignment (recommended).\n"
                + "Warning: wrong values may cause crashes and loss of data!";

        Property propDragonEntityID = config.get(CATEGORY_NAME_DEBUG, "dragonEntityID", DEFAULT_ENTITY_ID_AUTOASSIGN,
                ENTITY_ID_COMMENT, MIN_ENTITY_ID, MAX_ENTITY_ID);
        propDragonEntityID.setLanguageKey("gui.config.debug.dragon_entity_id");

        final boolean DEFAULT_DEBUG = false;
        Property propDebug = config.get(CATEGORY_NAME_DEBUG, "debug", DEFAULT_DEBUG);
        propDebug.comment = "Debug mode. Unless you're a developer or are told to activate it, you don't want to set this to true.";
        propDebug.setLanguageKey("gui.config.debug.debug");

        //By defining a property order we can control the order of the properties in the config file and GUI
        //This is defined on a per config-category basis
        List<String> propOrderOptions = new ArrayList<String>();
        propOrderOptions.add(propOrbHighlightTarget.getName()); //push the config value's name into the ordered list
        propOrderOptions.add(propOrbTargetAutoLock.getName());
        propOrderOptions.add(propOrbHolderImmune.getName());
        propOrderOptions.add(propEggsInChests.getName());
        config.setCategoryPropertyOrder(CATEGORY_NAME_OPTIONS, propOrderOptions);

        List<String> propOrderDebug = new ArrayList<String>();
        propOrderDebug.add(propDragonEntityID.getName());
        propOrderDebug.add(propDebug.getName());
        config.setCategoryPropertyOrder(CATEGORY_NAME_DEBUG, propOrderDebug);

        // ---- step 3 - read the configuration property values into the class's variables (if readFieldsFromConfig) -------------------
        // As each value is read from the property, it should be checked to make sure it is valid, in case someone
        //   has manually edited or corrupted the value.  The get() methods don't check that the value is in range even
        //   if you have specified a MIN and MAX value of the property
        if (readFieldsFromConfig) {
            //If getInt cannot get an integer value from the config file value of myInteger (e.g. corrupted file)
            // it will set it to the default value passed to the function

            eggsInChests = propEggsInChests.getBoolean(DEFAULT_EGGS_IN_CHEST);
            orbTargetAutoLock = propOrbTargetAutoLock.getBoolean(DEFAULT_AUTOLOCK);
            orbHighlightTarget = propOrbHighlightTarget.getBoolean(DEFAULT_HIGHLIGHT);
            orbHolderImmune = propOrbHolderImmune.getBoolean(DEFAULT_IMMUNE);
            dragonEntityID = propDragonEntityID.getInt(DEFAULT_ENTITY_ID_AUTOASSIGN);
            if (dragonEntityID > MAX_ENTITY_ID || dragonEntityID < MIN_ENTITY_ID) {
                dragonEntityID = DEFAULT_ENTITY_ID_AUTOASSIGN;
            }
            debug = propDebug.getBoolean(DEFAULT_DEBUG);
        }

        // ---- step 4 - write the class's variables back into the config properties and save to disk -------------------
        //  This is done even for a loadFromFile==true, because some of the properties may have been assigned default
        //    values if the file was empty or corrupt.
        propEggsInChests.set(eggsInChests);
        propOrbTargetAutoLock.set(orbTargetAutoLock);
        propOrbHighlightTarget.set(orbHighlightTarget);
        propOrbHolderImmune.set(orbHolderImmune);
        propDragonEntityID.set(dragonEntityID);
        propDebug.set(debug);

        if (config.hasChanged()) {
            config.save();
        }
    }

    //Define your configuration object
    private Configuration config = null;

    public class ConfigEventHandler {
        /*
         * This class, when instantiated as an object, will listen on the FML
         *  event bus for an OnConfigChangedEvent
         */
        @SubscribeEvent(priority = EventPriority.NORMAL)
        public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (DragonMounts.ID.equals(event.modID)
                    && !event.isWorldRunning) {
                if (event.configID.equals(CATEGORY_NAME_OPTIONS) || event.configID.equals(CATEGORY_NAME_DEBUG)) {
                    syncFromGUI();
                }
            }
        }
    }
}
