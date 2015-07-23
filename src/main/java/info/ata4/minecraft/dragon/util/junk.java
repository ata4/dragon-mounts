//package info.ata4.minecraft.dragon.util;
//
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.OpenGlHelper;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.util.MathHelper;
//
///**
// * Created by Richard on 23/07/2015.
// */
//public class junk
//{
//  public void doRender(EntityLivingBase entity, double x, double y, double z, float rotationYaw, float partialTicks)
//  {
//    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre(entity, this, x, y, z))) return;
//    GlStateManager.pushMatrix();
//    GlStateManager.disableCull();
//    this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
//    this.mainModel.isRiding = entity.isRiding();
//    this.mainModel.isChild = entity.isChild();
//
//    try
//    {
//      float renderYawOffset = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
//      float rotationYawHead = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
//      float netHeadYaw = rotationYawHead - renderYawOffset;
//      float netHeadYawClamp;
//
//      if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase)
//      {
//        EntityLivingBase entitylivingbase1 = (EntityLivingBase)entity.ridingEntity;
//        renderYawOffset = this.interpolateRotation(entitylivingbase1.prevRenderYawOffset, entitylivingbase1.renderYawOffset, partialTicks);
//        netHeadYaw = rotationYawHead - renderYawOffset;
//        netHeadYawClamp = MathHelper.wrapAngleTo180_float(netHeadYaw);
//
//        if (netHeadYawClamp < -85.0F)
//        {
//          netHeadYawClamp = -85.0F;
//        }
//
//        if (netHeadYawClamp >= 85.0F)
//        {
//          netHeadYawClamp = 85.0F;
//        }
//
//        renderYawOffset = rotationYawHead - netHeadYawClamp;
//
//        if (netHeadYawClamp * netHeadYawClamp > 2500.0F)
//        {
//          renderYawOffset += netHeadYawClamp * 0.2F;
//        }
//      }
//
//      float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
//      this.renderLivingAt(entity, x, y, z);
//      netHeadYawClamp = this.handleRotationFloat(entity, partialTicks);
//      this.rotateCorpse(entity, netHeadYawClamp, renderYawOffset, partialTicks);
//      GlStateManager.enableRescaleNormal();
//      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
//      this.preRenderCallback(entity, partialTicks);
//      float f6 = 0.0625F;
//      GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
//      float limbSwingAmount = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
//      float limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
//
//      if (entity.isChild())
//      {
//        limbSwing *= 3.0F;
//      }
//
//      if (limbSwingAmount > 1.0F)
//      {
//        limbSwingAmount = 1.0F;
//      }
//
//      GlStateManager.enableAlpha();
//      this.mainModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
//      this.mainModel.setRotationAngles(limbSwing, limbSwingAmount, netHeadYawClamp, netHeadYaw, pitch, 0.0625F, entity);
//      boolean flag;
//
//      if (this.renderOutlines)
//      {
//        flag = this.func_177088_c(entity);
//        this.renderModel(entity, limbSwing, limbSwingAmount, netHeadYawClamp, netHeadYaw, pitch, 0.0625F);
//
//        if (flag)
//        {
//          this.func_180565_e();
//        }
//      }
//      else
//      {
//        flag = this.func_177090_c(entity, partialTicks);
//        this.renderModel(entity, limbSwing, limbSwingAmount, netHeadYawClamp, netHeadYaw, pitch, 0.0625F);
//
//        if (flag)
//        {
//          this.func_177091_f();
//        }
//
//        GlStateManager.depthMask(true);
//
//        if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
//        {
//          this.func_177093_a(entity, limbSwing, limbSwingAmount, partialTicks, netHeadYawClamp, netHeadYaw, pitch, 0.0625F);
//        }
//      }
//
//      GlStateManager.disableRescaleNormal();
//    }
//    catch (Exception exception)
//    {
//      logger.error("Couldn\'t render entity", exception);
//    }
//
//    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
//    GlStateManager.enableTexture2D();
//    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
//    GlStateManager.enableCull();
//    GlStateManager.popMatrix();
//
//    if (!this.renderOutlines)
//    {
//      super.doRender(entity, x, y, z, rotationYaw, partialTicks);
//    }
//    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post(entity, this, x, y, z));
//  }
//}
