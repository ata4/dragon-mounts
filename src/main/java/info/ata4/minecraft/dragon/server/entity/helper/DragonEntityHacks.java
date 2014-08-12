/*
 ** 2014 February 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import cpw.mods.fml.relauncher.ReflectionHelper;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.DragonBodyHelper;
import info.ata4.minecraft.dragon.server.util.PrivateFields;
import net.minecraft.entity.EntityLiving;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HAAAAAX! Workaround code for unsolved bugs and vanilla design failures.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonEntityHacks extends DragonHelper {
    
    private static final Logger L = LogManager.getLogger();
    
    public DragonEntityHacks(EntityTameableDragon dragon) {
        super(dragon);
        
        // override EntityBodyHelper field, which is private and has no setter
        // required to fixate body while sitting. also slows down rotation while standing.
        try {
            ReflectionHelper.setPrivateValue(EntityLiving.class, dragon, new DragonBodyHelper(dragon), PrivateFields.ENTITYLIVING_BODYHELPER);
        } catch (Exception ex) {
            L.warn("Can't override EntityBodyHelper", ex);
        }
    }
}
