/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon;

import info.ata4.minecraft.render.GLUtils;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Generic renderer for all dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonRenderer extends RenderLiving {
    
    private static final String TEX_SADDLE = "/mob/dragon/saddle.png";
    private static final String TEX_EYES = "/mob/dragon/eyes.png";
    private static final String TEX_OVERLAY = "/mob/dragon/power.png";
    private static final String TEX_SHUFFLE = "/mob/dragon/shuffle.png";

    public DragonRenderer(ModelBase model) {
        super(model, 0.5f);
        renderPassModel = model;
    }
    
    @Override
    public void doRenderLiving(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {        
        Dragon dragon = (Dragon) entity;
        
        // debug hitbox rendering
        if (dragon.renderHitbox) {
            GLUtils.renderAABB(dragon.boundingBox, x - dragon.lastTickPosX, y - dragon.lastTickPosY, z - dragon.lastTickPosZ);
        }
  
        // debug target position rendering
        if (entity instanceof VolantDragon) {
            VolantDragon dragonVolant = (VolantDragon) entity;
            if (dragonVolant.renderTarget) {
                GLUtils.renderAxes(x - (dragonVolant.posX - dragonVolant.targetX),
                        y - (dragonVolant.posY - dragonVolant.targetY),
                        z - (dragonVolant.posZ - dragonVolant.targetZ));
            }
        }
        
        super.doRenderLiving(dragon, x, y, z, yaw, partialTicks);
    }

    @Override
    public int shouldRenderPass(EntityLiving entity, int pass, float scale) {
        Dragon dragon = (Dragon) entity;
        
        if (pass == 0 && dragon.updateModel && dragon.ticksExisted % 20 == 0) {
            mainModel = renderPassModel = new DragonModel();
        }
        
        ((DragonModel)renderPassModel).renderPass = pass;

        switch (pass) {
            // pass 1 - saddle
            case 0:
                if (dragon.isSaddled()) {
                    loadTexture(TEX_SADDLE);
                    return 1;
                }
                break;
            
            // pass 2 - glowing eyes
            case 1:
                loadTexture(TEX_EYES);

                GL11.glDisable(GL11.GL_ALPHA_TEST);
                
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                
                GL11.glDepthFunc(GL11.GL_EQUAL);

                GL11.glDisable(GL11.GL_LIGHTING);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapEnabled, 0, 255);
                GL11.glEnable(GL11.GL_LIGHTING);
                
                return 1;
            
            // pass 3 - noclip overlay effect
            case 2:
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                
                if (dragon.showOverlay()) {
                    loadTexture(TEX_OVERLAY);
                    
                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glLoadIdentity();
                    
                    float shift = (float) dragon.ticksExisted + scale;
                    float shiftX = shift * 0.015f;
                    float shiftY = shift * 0.035f;
                    
                    GL11.glTranslatef(shiftX, shiftY, 0);
                    GL11.glScalef(1.3f, 1.3f, 1.3f);
                    
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    GL11.glEnable(GL11.GL_BLEND);
                    
                    float c = 0.75f;
                    GL11.glColor4f(c, c, c, 1);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                    
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapEnabled, 0, 255);
                    
                    return 1;
                }
                break;
             
            // pass 4 - reset noclip effect
            case 3:
                if (dragon.showOverlay()) {
                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glLoadIdentity();
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glDisable(GL11.GL_BLEND);
                }
                break;
        }
        
        return -1;
    }

    @Override
    protected void rotateCorpse(EntityLiving entity, float ticksExisting, float pitch, float partialTicks) {
        Dragon dragon = (Dragon) entity;
        
        float yOfs = 2.5f * dragon.getBodySize();
        float yRot = -(float) dragon.getTrail(DragonModel.VERTS_TAIL - DragonModel.VERTS_NECK, partialTicks)[0];
        float xRot = (float) (dragon.getTrail(DragonModel.VERTS_NECK, partialTicks)[1] - dragon.getTrail(DragonModel.VERTS_TAIL - 2, partialTicks)[1]) * DragonModel.NECK_SIZE;
        
        GL11.glTranslatef(0, yOfs, 0);
        GL11.glRotatef(yRot, 0, 1, 0);
        GL11.glRotatef(xRot, 1, 0, 0);
        
        // partices won't spawn at the right position
//        if (dragon.onGround && dragon.deathTime > 0) {
//            float deathRotProg = (((dragon.deathTime + f2) - 1) / 20f) * 1.6f;
//            deathRotProg = MathHelper.sqrt_float(deathRotProg);
//            if (deathRotProg > 1) {
//                deathRotProg = 1;
//            }
//            GL11.glTranslatef(0, deathRotProg, 0);
//            GL11.glRotatef(deathRotProg * getDeathMaxRotation(dragon), 0, 0, 1);
//        }
    }
    
    @Override
    protected void renderModel(EntityLiving entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Dragon dragon = (Dragon) entity;
        ((DragonModel)renderPassModel).renderPass = -1;

        if (dragon.getDeathTime() > 0) {
            float f6 = dragon.getDeathTime() / (float) dragon.getMaxDeathTime();
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(516, f6);
            loadDownloadableImageTexture(dragon.skinUrl, TEX_SHUFFLE);
            mainModel.render(dragon, f, f1, f2, f3, f4, f5);
            GL11.glAlphaFunc(516, 0.1f);
            GL11.glDepthFunc(GL11.GL_EQUAL);
        }

        super.renderModel(entity, f, f1, f2, f3, f4, f5);
    }

    @Override
    protected void preRenderCallback(EntityLiving entity, float f) {
        Dragon dragon = (Dragon) entity;
        
        float size = dragon.getBodySize();
        
        if (size != 1) {
            GL11.glScalef(size, size, size);
        }
    }
}
