package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

/**
 * Created by EveryoneElse on 14/06/2015.
 */
public class LayerRendererDragonGlow implements LayerRenderer {
  private final DragonRenderer dragonRenderer;

  public LayerRendererDragonGlow(DragonRenderer i_dragonRenderer)
  {
    dragonRenderer = i_dragonRenderer;
  }

  public void doRenderLayer(EntityTameableDragon entityDragon, float moveTime, float moveSpeed, float partialTicks,
                            float ticksExisted, float lookYaw, float lookPitch, float scale)
  {
    DragonModel dragonModel = dragonRenderer.getModel();
    dragonModel.renderPass = DragonModel.RenderPass.GLOW;
    dragonRenderer.bindTexture(dragonModel.glowTexture);

    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
    GL11.glDisable(GL11.GL_LIGHTING);      // use full lighting
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 65536, 0);

    dragonModel.render(entityDragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);

    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glDisable(GL11.GL_BLEND);
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
