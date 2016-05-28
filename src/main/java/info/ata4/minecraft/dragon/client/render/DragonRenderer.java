/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.client.model.DragonModelMode;
import info.ata4.minecraft.dragon.client.render.breeds.DefaultDragonBreedRenderer;
import info.ata4.minecraft.dragon.server.block.BlockDragonBreedEgg;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStageHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Generic renderer for all dragons.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonRenderer extends RenderLiving<EntityTameableDragon> {

    public static final String TEX_BASE = "textures/entities/dragon/";
    
    private final Map<EnumDragonBreed, DefaultDragonBreedRenderer> breedRenderers = new EnumMap<>(EnumDragonBreed.class);

    public DragonRenderer(RenderManager renderManager) {
        super(renderManager, null, 2);

        // create default breed renderers
        for (EnumDragonBreed breed : EnumDragonBreed.values()) {
            if (!breedRenderers.containsKey(breed)) {
                breedRenderers.put(breed, new DefaultDragonBreedRenderer(this, breed));
            }
        }
    }
    
    public DefaultDragonBreedRenderer getBreedRenderer(EntityTameableDragon dragon) {
        return breedRenderers.get(dragon.getBreedType());
    }

    @Override
    public void doRender(EntityTameableDragon dragon, double x, double y, double z, float yaw, float partialTicks) {
        DragonModel breedModel = getBreedRenderer(dragon).getModel();
        breedModel.setMode(DragonModelMode.FULL);
        mainModel = breedModel;
        renderName(dragon, x, y, z);

        if (dragon.isEgg()) {
            renderEgg(dragon, x, y, z, yaw, partialTicks);
        } else {
            super.doRender(dragon, x, y, z, yaw, partialTicks);
        }
    }

    @Override
    protected void renderLayers(EntityTameableDragon dragon, float moveTime,
            float moveSpeed, float partialTicks, float ticksExisted, float lookYaw,
            float lookPitch, float scale) {
        List<LayerRenderer<EntityTameableDragon>> layers = getBreedRenderer(dragon).getLayers();
        layers.forEach(layer -> {
            boolean brighnessSet = setBrightness(dragon, partialTicks,
                    layer.shouldCombineTextures());
            layer.doRenderLayer(dragon, moveTime, moveSpeed, partialTicks,
                    ticksExisted, lookYaw, lookPitch, scale);
            if (brighnessSet) {
                unsetBrightness();
            }
        });
    }

    /**
     * Renders the model in RenderLiving
     */
    @Override
    protected void renderModel(EntityTameableDragon dragon, float moveTime, float moveSpeed,
            float ticksExisted, float lookYaw, float lookPitch, float scale) {
        
        float death = dragon.getDeathTime() / (float) dragon.getMaxDeathTime();

        if (death > 0) {
            glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
            
            GlStateManager.depthFunc(GL_LEQUAL);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL_GREATER, death);

            bindTexture(getBreedRenderer(dragon).getDissolveTexture());
            mainModel.render(dragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);

            GlStateManager.alphaFunc(GL_GREATER, 0.1f);
            GlStateManager.depthFunc(GL_EQUAL);
        }

        super.renderModel(dragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
        
        if (death > 0) {
            GlStateManager.popAttrib();
        }
    }

    protected void renderEgg(EntityTameableDragon dragon, double x, double y, double z, float pitch, float partialTicks) {
        // apply egg wiggle
        DragonLifeStageHelper lifeStage = dragon.getLifeStageHelper();
        float tickX = lifeStage.getEggWiggleX();
        float tickZ = lifeStage.getEggWiggleZ();

        float rotX = 0;
        float rotZ = 0;

        if (tickX > 0) {
            rotX = (float) Math.sin(tickX - partialTicks) * 8;
        }
        if (tickZ > 0) {
            rotZ = (float) Math.sin(tickZ - partialTicks) * 8;
        }

        // prepare GL states
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(rotX, 1, 0, 0);
        GlStateManager.rotate(rotZ, 0, 0, 1);
        GlStateManager.disableLighting();
        
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // prepare egg rendering
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vb = tessellator.getBuffer();
        vb.begin(GL_QUADS, DefaultVertexFormats.BLOCK);

        Block block = BlockDragonBreedEgg.INSTANCE;
        IBlockState iblockstate = block.getDefaultState().withProperty(
                BlockDragonBreedEgg.BREED, dragon.getBreedType());
        BlockPos blockpos = dragon.getPosition();
        
        double tx = -blockpos.getX() - 0.5;
        double ty = -blockpos.getY();
        double tz = -blockpos.getZ() - 0.5;
        vb.setTranslation(tx, ty, tz);
        
        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel bakedModel = brd.getModelForState(iblockstate);
        
        // render egg
        brd.getBlockModelRenderer().renderModel(dragon.worldObj, bakedModel,
                iblockstate, blockpos, vb, false);
        vb.setTranslation(0, 0, 0);
        
        tessellator.draw();
        
        // restore GL state
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    @Override
    protected void rotateCorpse(EntityTameableDragon dragon, float par2, float par3, float par4) {
        GlStateManager.rotate(180 - par3, 0, 1, 0);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before
     * the model is rendered. Args: entityLiving, partialTickTime
     */
    @Override
    protected void preRenderCallback(EntityTameableDragon dragon, float partialTicks) {
        float scale = dragon.getScale() * 0.8f;
        GlStateManager.scale(scale, scale, scale);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTameableDragon dragon) {
        return getBreedRenderer(dragon).getBodyTexture();
    }
}
