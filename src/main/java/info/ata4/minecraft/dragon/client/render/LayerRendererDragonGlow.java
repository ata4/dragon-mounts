package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by EveryoneElse on 14/06/2015.
 */
public class LayerRendererDragonGlow extends LayerRendererDragon {

    public LayerRendererDragonGlow(DragonRenderer dragonRenderer) {
        super(dragonRenderer);
    }

    @Override
    public void doRenderLayer(EntityTameableDragon dragon, float moveTime,
            float moveSpeed, float partialTicks, float ticksExisted, float lookYaw,
            float lookPitch, float scale) {
        DragonModel dragonModel = dragonRenderer.getModel();
        dragonRenderer.bindTexture(dragonModel.glowTexture);
        
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_ONE, GL_ONE);
        GlStateManager.disableLighting();

        int b = 61680;
        int u = b % 65536;
        int v = b / 65536;
        
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, u, v);
        GlStateManager.color(1, 1, 1, 1);

        dragonModel.render(dragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
        
        b = dragon.getBrightnessForRender(partialTicks);
        u = b % 65536;
        v = b / 65536;
        
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, u, v);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
