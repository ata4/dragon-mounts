/*
 ** 2012 August 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.gui.GuiDragonDebug;
import info.ata4.minecraft.dragon.client.handler.DragonControl;
import info.ata4.minecraft.dragon.client.handler.DragonOrbControl;
import info.ata4.minecraft.dragon.client.handler.TextureStitcherBreathFX;
import info.ata4.minecraft.dragon.client.render.DragonRenderer;
import info.ata4.minecraft.dragon.server.CommonProxy;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void onPreInit(FMLPreInitializationEvent evt)
    {
      super.onPreInit(evt);
      MinecraftForge.EVENT_BUS.register(new TextureStitcherBreathFX());
    }

    @Override
    public void onInit(FMLInitializationEvent evt) {
        super.onInit(evt);
      ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("dragonmounts:dragonorb", "inventory");
      final int DEFAULT_ITEM_SUBTYPE = 0;
      Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemDragonOrb, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
    }

    @Override
    public void onPostInit(FMLPostInitializationEvent event)
    {
      if (DragonMounts.instance.getConfig().isDebug()) {
        MinecraftForge.EVENT_BUS.register(new GuiDragonDebug());
      }

      RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
      RenderingRegistry.registerEntityRenderingHandler(EntityTameableDragon.class, new DragonRenderer(renderManager));

        FMLCommonHandler.instance().bus().register(new DragonControl(getNetwork()));
      FMLCommonHandler.instance().bus().register(DragonOrbControl.getInstance());

    }

  /**
   * returns the EntityPlayerSP if this is the client, otherwise returns null.
   * @return
   */
    @Override
    public Entity getClientEntityPlayerSP()
    {
      return Minecraft.getMinecraft().thePlayer;
    }
}
