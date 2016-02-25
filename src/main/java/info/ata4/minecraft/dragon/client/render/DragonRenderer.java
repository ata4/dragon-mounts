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

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.DragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.DragonBreedRegistry;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStageHelper;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

/**
 * Generic renderer for all dragons.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonRenderer extends RenderLiving<EntityTameableDragon> {

    public static final String TEX_BASE = "textures/entities/dragon/";
    
    public static boolean updateModel;

    private final Map<DragonBreed, DragonModel> breedModels = new HashMap<DragonBreed, DragonModel>();
    private final ResourceLocation dissolveTextureLoc = new ResourceLocation(DragonMounts.AID, DragonRenderer.TEX_BASE + "dissolve.png");

    private DragonModel dragonModel;

    public DragonRenderer(RenderManager renderManager) {
        super(renderManager, null, 2);
        
        // create render layers
        addLayer(new LayerRendererDragonSaddle(this));
        addLayer(new LayerRendererDragonGlow(this));

        // create a separate model for each breed
        breedModels.clear();
        for (DragonBreed breed : DragonBreedRegistry.getInstance().getBreeds()) {
            breedModels.put(breed, new DragonModel(breed));
        }
    }

    private void setModelForBreed(DragonBreed breed) {
        mainModel = dragonModel = breedModels.get(breed);
    }

    public DragonModel getModel() {
        return dragonModel;
    }

    @Override
    public void doRender(EntityTameableDragon dragon, double x, double y, double z, float yaw, float partialTicks) {
        setModelForBreed(dragon.getBreed());
        renderName(dragon, x, y, z);

        if (dragon.isEgg()) {
            renderEgg(dragon, x, y, z, yaw, partialTicks);
        } else {
            super.doRender(dragon, x, y, z, yaw, partialTicks);
        }
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

            bindTexture(dissolveTextureLoc);
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
        
        bindTexture(TextureMap.locationBlocksTexture);
        
        // prepare egg rendering
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.BLOCK);

        Block block = Blocks.dragon_egg;
        IBlockState iblockstate = block.getDefaultState();
        BlockPos blockpos = new BlockPos(dragon);
        
        double tx = -blockpos.getX() - 0.5;
        double ty = -blockpos.getY();
        double tz = -blockpos.getZ() - 0.5;
        worldRenderer.setTranslation(tx, ty, tz);
        
        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel ibakedmodel = brd.getModelFromBlockState(iblockstate, dragon.worldObj, null);

        // render egg
        brd.getBlockModelRenderer().renderModel(dragon.worldObj, ibakedmodel, iblockstate, blockpos, worldRenderer, false);
        worldRenderer.setTranslation(0, 0, 0);
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
        return dragonModel.bodyTexture;
    }
}
