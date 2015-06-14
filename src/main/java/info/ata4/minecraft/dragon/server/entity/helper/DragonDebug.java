/*
 ** 2013 October 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonDebug extends DragonHelper {
    
    private static final Logger L = LogManager.getLogger();
    
    public DragonDebug(EntityTameableDragon dragon) {
        super(dragon);
    }
    
    private void dumpNBT(NBTTagCompound nbt) {
        File dumpFile = new File(Minecraft.getMinecraft().mcDataDir,
                String.format("dragon_%08x.nbt", dragon.getEntityId()));
        
        try {
            CompressedStreamTools.write(nbt, dumpFile);
        } catch (IOException ex) {
            L.warn("Can't dump NBT", ex);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        L.debug("writeToNBT");
        dumpNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        L.debug("readFromNBT");
        dumpNBT(nbt);
    }

    @Override
    public void onDeath() {
        L.debug("onDeath");
    }
}
