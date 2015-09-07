package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

/**
 * Created by TGG on 14/06/2015.
 * Render the dragon's saddle.
 */
public class LayerRendererDragonSaddle implements LayerRenderer {
  private final DragonRenderer dragonRenderer;

  public LayerRendererDragonSaddle(DragonRenderer i_dragonRenderer)
  {
    dragonRenderer = i_dragonRenderer;
  }

  public void doRenderLayer(EntityTameableDragon entityDragon, float moveTime, float moveSpeed, float partialTicks,
                            float ticksExisted, float lookYaw, float lookPitch, float scale)
  {
    if (!entityDragon.isSaddled()) return;
    DragonModel dragonModel = dragonRenderer.getModel();
    dragonModel.renderPass = DragonModel.RenderPass.SADDLE;
    dragonRenderer.bindTexture(dragonModel.saddleTexture);
    dragonModel.render(entityDragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
  }

  public boolean shouldCombineTextures()
  {
    return false;
  }

  public void doRenderLayer(EntityLivingBase entityLivingBase, float moveTime, float moveSpeed, float partialTicks,
                            float ticksExisted, float lookYaw, float lookPitch, float scale)
  {
    this.doRenderLayer((EntityTameableDragon)entityLivingBase, moveTime, moveSpeed, partialTicks,
                       ticksExisted, lookYaw, lookPitch, scale);
  }
}
