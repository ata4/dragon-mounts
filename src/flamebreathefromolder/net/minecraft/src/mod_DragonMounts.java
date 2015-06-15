/*
** 2011 December 22
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package net.minecraft.src;

import info.ata4.log.LogUtils;
import info.ata4.minecraft.dragon.DragonModel;
import info.ata4.minecraft.dragon.RidableVolantDragon;
import info.ata4.minecraft.dragon.DragonRenderer;
import info.ata4.minecraft.dragonegg.DragonEggBlock;
import info.ata4.minecraft.dragon.DragonPartRenderer;
import info.ata4.minecraft.render.RenderInvisible;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class mod_DragonMounts extends BaseMod {
    
    private static final Logger L = Logger.getLogger(mod_DragonMounts.class.getName());
    
    @MLProp(
        name = "control_mode",
        info = "Sets the control method for dragons:\n"
        + "0 - Mouse look control\n"
        + "1 - Keyboard control\n"
        + "2 - Combined mouse/keyboard control",
        min = 0,
        max = 2
    )
    public static int controlMode = 0;
    
    @MLProp(
        name = "logging_level",
        info = "Logging level for this mod. Set to ALL for full debug output."
    )
    public static String loggingLevel = "INFO";
    
    public mod_DragonMounts() {
        Level logLevel;
        
        try {
            logLevel = Level.parse(loggingLevel);
        } catch (IllegalArgumentException ex) {
            // PEBKAC
            logLevel = Level.INFO;
        }
        
        LogUtils.configure(L, logLevel);
    }
    
    @Override
    public String getName() {
        return "Dragon Mounts";
    }
    
    @Override
    public String getVersion() {
        return "0.6";
    }
    
    public static Logger getLogger() {
        return L;
    }
    
    @Override
    public void AddRenderer(Map map) {
        map.put(info.ata4.minecraft.dragon.Dragon.class, new DragonRenderer(new DragonModel()));
        map.put(info.ata4.minecraft.dragon.DragonPart.class, new DragonPartRenderer());
        map.put(info.ata4.minecraft.flame.FlameEmitter.class, new RenderInvisible());
    }

    @Override
    public void load() {
        ModLoader.RegisterEntityID(RidableVolantDragon.class, "DragonMount", ModLoader.getUniqueEntityId());

        // unregister original block ID so the constructor won't complain
        Block.blocksList[Block.dragonEgg.blockID] = null;
        
        Block dragonEggOld = Block.dragonEgg;
        Block dragonEgg = new DragonEggBlock(
            dragonEggOld.blockID, dragonEggOld.blockIndexInTexture)
            .setHardness(dragonEggOld.blockHardness)
            .setResistance(dragonEggOld.blockResistance)
            .setStepSound(dragonEggOld.stepSound)
            .setLightValue(0.125f)
            .setBlockName(dragonEggOld.getBlockName().replaceFirst("^tile\\.", ""));
        
        ModLoader.RegisterBlock(dragonEgg);
        
        // override final Block.dragonEgg object so the Dragon Egg entity is displayed correctly
        try {
            ModLoader.setPrivateValue(net.minecraft.src.Block.class, null, 139, dragonEgg);
        } catch (Exception ex) {
            // Alright. So that was... seriously disappointing. 
            L.log(Level.WARNING, "Couldn't override dragon egg block!", ex);
        }
        
        ModLoader.AddLocalization("mod.dragonmount.unsaddled", "You need to saddle your dragon to control it properly!");
        ModLoader.AddLocalization("mod.dragonmount.noroom", "Not enough room to spawn a dragon!");
    }
}
