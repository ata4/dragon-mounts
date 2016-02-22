package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/**
 * Created by EveryoneElse on 14/06/2015.
 */
public class LayerRendererDragonSaddle implements LayerRenderer<EntityTameableDragon> {

    private final DragonRenderer dragonRenderer;

    public LayerRendererDragonSaddle(DragonRenderer dragonRenderer) {
        this.dragonRenderer = dragonRenderer;
    }

    @Override
    public void doRenderLayer(EntityTameableDragon entityDragon, float moveTime,
            float moveSpeed, float partialTicks, float ticksExisted, float lookYaw,
            float lookPitch, float scale) {
        if (!entityDragon.isSaddled()) {
            return;
        }
        
        DragonModel dragonModel = dragonRenderer.getModel();
        dragonModel.renderPass = DragonModel.RenderPass.SADDLE;
        dragonRenderer.bindTexture(dragonModel.saddleTexture);
        dragonModel.render(entityDragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
