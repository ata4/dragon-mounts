package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;

/**
 * Created by EveryoneElse on 14/06/2015.
 */
public class LayerRendererDragonSaddle extends LayerRendererDragon {

    public LayerRendererDragonSaddle(DragonRenderer dragonRenderer) {
        super(dragonRenderer);
    }
    
    @Override
    public void doRenderLayer(EntityTameableDragon dragon, float moveTime,
            float moveSpeed, float partialTicks, float ticksExisted, float lookYaw,
            float lookPitch, float scale) {
        if (!dragon.isSaddled()) {
            return;
        }
        
        DragonModel dragonModel = dragonRenderer.getModel();
        dragonRenderer.bindTexture(dragonModel.saddleTexture);
        dragonModel.render(dragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
