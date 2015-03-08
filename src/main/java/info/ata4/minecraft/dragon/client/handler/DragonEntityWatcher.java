/*
 ** 2012 August 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.handler;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.reflection.PrivateFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Event handler that watches the currently ridden dragon entity. Used to change
 * the camera distance and to show button usage notices.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonEntityWatcher {
    
    private final Minecraft mc = Minecraft.getMinecraft();
    private final float defaultThirdPersonDistance;
    private int noticeTicks;
    private boolean ridingDragon;
    private boolean ridingDragonPrev;
    
    public DragonEntityWatcher() {
        defaultThirdPersonDistance = getThirdPersonDistance();
    }
    
    private float getThirdPersonDistance() {
        return ReflectionHelper.getPrivateValue(EntityRenderer.class, mc.entityRenderer, PrivateFields.ENTITYRENDERER_THIRDPERSONDISTANCE);
    }
    
    private void setThirdPersonDistance(float thirdPersonDistance) {
        ReflectionHelper.setPrivateValue(EntityRenderer.class, mc.entityRenderer, thirdPersonDistance, PrivateFields.ENTITYRENDERER_THIRDPERSONDISTANCE);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) {
            return;
        }
        
        if (mc.thePlayer == null) {
            return;
        }

        ridingDragon = mc.thePlayer.ridingEntity instanceof EntityTameableDragon;
        
        // display a key binding notice after the vanilla notice
        if (ridingDragon && !ridingDragonPrev) {
            setThirdPersonDistance(6);
            noticeTicks = 70;
        } else if (!ridingDragon && ridingDragonPrev) {
            setThirdPersonDistance(defaultThirdPersonDistance);
            noticeTicks = 0;
        } else {
            if (noticeTicks > 0) {
                noticeTicks--;
            }
            
            if (noticeTicks == 1) {
                String keyUpName = GameSettings.getKeyDisplayString(DragonControl.KEY_FLY_UP.getKeyCode());
                String keyDownName = GameSettings.getKeyDisplayString(DragonControl.KEY_FLY_DOWN.getKeyCode());
                mc.ingameGUI.setRecordPlaying(I18n.format("dragon.mountNotice", new Object[] {keyUpName, keyDownName}), false);
            }
        }
        
        ridingDragonPrev = ridingDragon;
    }
}
