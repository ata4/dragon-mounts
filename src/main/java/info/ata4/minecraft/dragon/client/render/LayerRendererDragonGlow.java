package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by EveryoneElse on 14/06/2015.
 */
public class LayerRendererDragonGlow implements LayerRenderer<EntityTameableDragon> {

    private final DragonRenderer dragonRenderer;

    public LayerRendererDragonGlow(DragonRenderer dragonRenderer) {
        this.dragonRenderer = dragonRenderer;
    }

    @Override
    public void doRenderLayer(EntityTameableDragon entityDragon, float moveTime,
            float moveSpeed, float partialTicks, float ticksExisted, float lookYaw,
            float lookPitch, float scale) {
        DragonModel dragonModel = dragonRenderer.getModel();
        dragonModel.renderPass = DragonModel.RenderPass.GLOW;
        dragonRenderer.bindTexture(dragonModel.glowTexture);

        try {
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_ONE, GL_ONE);
            GlStateManager.disableLighting(); // use full lighting
            
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 65536, 0);

            dragonModel.render(entityDragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
        } finally {
            GlStateManager.popAttrib();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
