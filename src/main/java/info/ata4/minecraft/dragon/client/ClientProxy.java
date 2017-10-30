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
import info.ata4.minecraft.dragon.client.render.DragonRenderer;
import info.ata4.minecraft.dragon.server.CommonProxy;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClientProxy extends CommonProxy {


	@Override
	public void onPreInit(FMLPreInitializationEvent event) {
		super.onPreInit(event);

		// register dragon entity renderer
		RenderingRegistry.registerEntityRenderingHandler(
				EntityTameableDragon.class, DragonRenderer::new);

		// register item renderer for dragon egg block variants
		ResourceLocation eggModelItemLoc = new ResourceLocation(DragonMounts.AID, "dragon_egg");
		Item itemBlockDragonEgg = Item.REGISTRY.getObject(eggModelItemLoc);
		EnumDragonBreed.META_MAPPING.forEach((breed, meta) -> {
			ModelResourceLocation eggModelLoc = new ModelResourceLocation(DragonMounts.AID + ":dragon_egg", "breed=" + breed.getName());
			ModelLoader.setCustomModelResourceLocation(itemBlockDragonEgg, meta, eggModelLoc);
		});
	}

	@Override
	public void onInit(FMLInitializationEvent evt) {
		super.onInit(evt);
	}

	@Override
	public void onPostInit(FMLPostInitializationEvent event) {
		super.onPostInit(event);

		if (DragonMounts.instance.getConfig().isDebug()) {
			MinecraftForge.EVENT_BUS.register(new GuiDragonDebug());
		}
	}
}
