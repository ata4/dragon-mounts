package info.ata4.minecraft.dragon.client.handler;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by TGG on 19/06/2015.
 * Used to stitch the textures used by the breath weapon EntityFX into the blocks+items texture sheet, so that the
 *   EntityFX renderer can use them.
 */
public class TextureStitcherBreathFX
{
  @SubscribeEvent
  public void stitcherEventPre(TextureStitchEvent.Pre event) {
    ResourceLocation flameRL = new ResourceLocation("dragonmounts:entities/breath_fire");
    event.map.registerSprite(flameRL);
  }
}
