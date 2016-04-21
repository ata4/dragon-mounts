/*
** 2016 March 18
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon;

import java.util.Arrays;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMountsConfigGui extends GuiConfig {
    
    private static final Configuration CONFIG = DragonMounts.instance.getConfig().getParent();
    
    public DragonMountsConfigGui(GuiScreen parentScreen) {
        super(
            parentScreen,
            Arrays.asList(
                new ConfigElement(CONFIG.getCategory("server")),
                new ConfigElement(CONFIG.getCategory("client"))
            ),
            DragonMounts.ID,
            false,
            false,
            DragonMounts.NAME
        );
    }
    
}
