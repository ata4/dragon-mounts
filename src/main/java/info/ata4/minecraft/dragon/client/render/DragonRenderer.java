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
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import static org.lwjgl.opengl.GL11.*;

/**
 * Generic renderer for all dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonRenderer extends RenderLiving {
    
    public static final String TEX_BASE = "textures/entities/dragon/";
    public static final String MDL_BASE = "models/entities/dragon/";
    
    public static boolean updateModel;
    
    private final Map<DragonBreed, DragonModel> breedModels = new HashMap<DragonBreed, DragonModel>();
    private final ResourceLocation dissolveTexture = new ResourceLocation(DragonMounts.AID, TEX_BASE + "dissolve.png");
    private final ResourceLocation eggTexture = new ResourceLocation(DragonMounts.AID, MDL_BASE + "dragon_egg.obj");
    private final IModelCustom eggModel = AdvancedModelLoader.loadModel(eggTexture);
    
    private DragonModel dragonModel;

    public DragonRenderer() {
        super(null, 2);
        
        // create a separate model for each breed
        initBreedModels();
    }
    
    private void initBreedModels() {
        breedModels.clear();
        for (DragonBreed breed : DragonBreedRegistry.getInstance().getBreeds()) {
            breedModels.put(breed, new DragonModel(breed));
        }
    }
    
    private void setModel(DragonBreed breed) {
        mainModel = renderPassModel = dragonModel = breedModels.get(breed);
    }
    
    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
        doRender((EntityTameableDragon) entity, x, y, z, yaw, partialTicks);
    }
    
    public void doRender(EntityTameableDragon dragon, double x, double y, double z, float yaw, float partialTicks) {
        setModel(dragon.getBreed());
        passSpecialRender2(dragon, x, y, z);
        
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
    protected void renderModel(EntityLivingBase entity, float moveTime,
            float moveSpeed, float ticksExisted, float lookYaw, float lookPitch,
            float scale) {
        renderModel((EntityTameableDragon) entity, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
    }

    protected void renderModel(EntityTameableDragon dragon, float moveTime, float moveSpeed,
            float ticksExisted, float lookYaw, float lookPitch, float scale) {
        dragonModel.renderPass = -1;
        
        if (dragon.getDeathTime() > 0) {
            float alpha = dragon.getDeathTime() / (float) dragon.getMaxDeathTime();
            glDepthFunc(GL_LEQUAL);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, alpha);
            bindTexture(dissolveTexture);
            dragonModel.render(dragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
            glAlphaFunc(GL_GREATER, 0.1f);
            glDepthFunc(GL_EQUAL);
        }
        
        super.renderModel(dragon, moveTime, moveSpeed, ticksExisted, lookYaw, lookPitch, scale);
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
        
        // render block
        glPushMatrix();
        glTranslatef((float) x, (float) y, (float) z);
        glRotatef(rotX, 1, 0, 0);
        glRotatef(rotZ, 0, 0, 1);

        bindTexture(dragonModel.getEggTexture());
        eggModel.renderAll();
        
        glPopMatrix();
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    @Override
    public int shouldRenderPass(EntityLivingBase entity, int pass, float scale) {
        return shouldRenderPass((EntityTameableDragon) entity, pass, scale);
    }
    
    public int shouldRenderPass(EntityTameableDragon dragon, int pass, float scale) {
        // update dragon model every second if enabled
        if (pass == 0 && updateModel && dragon.ticksExisted % 20 == 0) {
            initBreedModels();
        }
        
        dragonModel.renderPass = pass;

        switch (pass) {
            // pass 1 - saddle
            case 0:
                if (dragon.isSaddled()) {
                    bindTexture(dragonModel.saddleTexture);
                    return 1;
                }
                break;
            
            // pass 2 - glow overlay
            case 1:
                bindTexture(dragonModel.glowTexture);

                // enable blending
                glEnable(GL_BLEND);
                glBlendFunc(GL_ONE, GL_ONE);

                // use full lighting
                glDisable(GL_LIGHTING);

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 65536, 0);

                return 1;
            
            // pass 3 - reset glow overlay
            case 2:
                glEnable(GL_LIGHTING);
                glDisable(GL_BLEND);
                break;
        }
        
        return -1;
    }
    
    @Override
    protected void rotateCorpse(EntityLivingBase par1EntityLiving, float par2, float par3, float par4) {
        rotateCorpse((EntityTameableDragon) par1EntityLiving, par2, par3, par4);
    }
    
    protected void rotateCorpse(EntityTameableDragon dragon, float par2, float par3, float par4) {
        glRotatef(180 - par3, 0, 1, 0);
    }
    
    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        preRenderCallback((EntityTameableDragon) entity, partialTicks);
    }
    
    protected void preRenderCallback(EntityTameableDragon dragon, float partialTicks) {
        float scale = dragon.getScale() * 0.8f;
        glScalef(scale, scale, scale);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return getEntityTexture((EntityTameableDragon) entity);
    }
    
    protected ResourceLocation getEntityTexture(EntityTameableDragon dragon) {
        return dragonModel.bodyTexture;
    }
    
    @Override
    protected void passSpecialRender(EntityLivingBase entity, double x, double y, double z) {
        // render the name label in doRender as a workaround for the incorrect
        // lighting when using model multipass rendering
    }
    
    protected void passSpecialRender2(EntityLivingBase par1EntityLiving, double par2, double par4, double par6) {
        super.passSpecialRender(par1EntityLiving, par2, par4, par6);
    }
}
