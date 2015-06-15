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

import info.ata4.minecraft.MathF;
import info.ata4.minecraft.model.ModelPartProxy;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;

/**
 * Generic model for all winged tetrapod dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonModel extends ModelBase {
    
    // model constants
    public static final int NECK_SIZE = 10;
    public static final int VERTS_NECK = 4;
    public static final int VERTS_TAIL = 12;
    public static final float[] WING_FOLD = new float[] {2.55f, 2.7f, 2.85f, 3f};
    public static final float[] WING_UNFOLD = new float[] {0.1f, 0.9f, 1.7f, 2.5f};
    
    // model parts
    private ModelRenderer head;
    private ModelRenderer neck;
    private ModelRenderer jaw;
    private ModelRenderer body;
    private ModelRenderer back;
    private ModelRenderer leg;
    private ModelRenderer legTip;
    private ModelRenderer foot;
    private ModelRenderer wingArm;
    private ModelRenderer wingForearm;
    private ModelRenderer wingFinger1;
    private ModelRenderer wingFinger2;
    private ModelRenderer wingFinger3;
    private ModelRenderer wingFinger4;
    
    // timing vars
    private float animTime;
    private float groundTime;
    private float flutterTime;
    private float cycle;
    private float cycleOfs;
    
    // model attributes
    private ModelPartProxy[] neckProxy = new ModelPartProxy[VERTS_NECK];
    private ModelPartProxy[] tailProxy = new ModelPartProxy[VERTS_TAIL];
    private ModelPartProxy[] legProxy = new ModelPartProxy[4];
    private ModelPartProxy[] legTipProxy = new ModelPartProxy[4];
    private ModelPartProxy[] footProxy = new ModelPartProxy[4];
    
    public int renderPass = -1;
    
    // animation attributes
    private boolean flutter;
    private boolean ground;
    private boolean mouthOpen;

    public DragonModel() {
        textureWidth = 384;
        textureHeight = 128;

        setTextureOffset("back.scale", 180, 48);
        setTextureOffset("body.body", 0, 0);
        setTextureOffset("body.scale", 0, 32);
        setTextureOffset("head.nostril", 139, 0);
        setTextureOffset("head.scale", 0, 0);
        setTextureOffset("head.upperhead", 0, 0);
        setTextureOffset("head.upperjaw", 56, 92);
        setTextureOffset("jaw.lowerjaw", 0, 92);
        setTextureOffset("leg.main", 112, 0);
        setTextureOffset("legtip.main", 349, 41);
        setTextureOffset("foot.main", 300, 0);
        setTextureOffset("neck.box", 338, 70);
        setTextureOffset("neck.scale", 48, 0);
        setTextureOffset("wingarm.bone", 112, 52);
        setTextureOffset("wingarm.skin", 276, 24);
        setTextureOffset("wingfinger.bone", 0, 88);
        setTextureOffset("wingfinger.shortskin", 176, 48);
        setTextureOffset("wingfinger.skin", 112, 0);
        setTextureOffset("wingforearm.bone", 176, 80);

        float headOfs = -16;

        head = new ModelRenderer(this, "head");
        head.addBox("upperjaw",  -6, -1,   -8 + headOfs, 12,  5, 16);
        head.addBox("upperhead", -8, -8,    6 + headOfs, 16, 16, 16);
        head.mirror = true;
        head.addBox("scale",     -5, -12,  12 + headOfs,  2,  4,  6);
        head.addBox("nostril",   -5,  -3,  -6 + headOfs,  2,  2,  4);
        head.mirror = false;
        head.addBox("scale",      3, -12,  12 + headOfs,  2,  4,  6);
        head.addBox("nostril",    3,  -3,  -6 + headOfs,  2,  2,  4);

        jaw = new ModelRenderer(this, "jaw");
        jaw.setRotationPoint(0, 4, 8 + headOfs);
        jaw.addBox("lowerjaw", -6, 0, -16, 12, 4, 16);
        head.addChild(jaw);

        neck = new ModelRenderer(this, "neck");
        neck.addBox("box",   -5,  -5,  -5, NECK_SIZE, NECK_SIZE, NECK_SIZE);
        neck.addBox("scale", -1,  -9,  -3, 2, 4, 6);

        body = new ModelRenderer(this, "body");
        body.setRotationPoint(0, 4, 8);
        body.addBox("body",  -12,   0,  -16, 24, 24, 64);
//        body.addBox("scale",  -1,  -6,  -10,  2,  6, 12);
        body.addBox("scale",  -1,  -6,   10,  2,  6, 12);
        body.addBox("scale",  -1,  -6,   30,  2,  6, 12);
        
        back = new ModelRenderer(this, "back");
        back.setRotationPoint(0, 0, 0);
        back.addBox("scale",  -1,  -6,  -10,  2,  6, 12);
        body.addChild(back);
        
        wingArm = new ModelRenderer(this, "wingarm");
        wingArm.setRotationPoint(-12, 5, 4);
        wingArm.addBox("bone",  -28,  -3,   -3, 28,  6,  6);
        wingArm.addBox("skin",  -28,   0,    2, 28,  0, 24);
        
        wingForearm = new ModelRenderer(this, "wingforearm");
        wingForearm.setRotationPoint(-28, 0, 0);
        wingForearm.addBox("bone", -48,  -2,  -2, 48,  4,  4);
        wingArm.addChild(wingForearm);
        
        wingFinger1 = buildWingFinger(false);
        wingFinger2 = buildWingFinger(false);
        wingFinger3 = buildWingFinger(false);
        wingFinger4 = buildWingFinger(true);
 
        // leg variables
        float legPosX = -14;
        float legPosY = 16;
        float legPosZ = 4;
        
        int legThick = 9;
        int legLength = 20;
        
        float legOfs = -(legThick / 2f);
        
        leg = new ModelRenderer(this, "leg");
        leg.setRotationPoint(legPosX, legPosY, legPosZ);
        leg.addBox("main", legOfs, legOfs, legOfs, legThick, legLength, legThick);
        
        // leg tip variables
        float legTipPosX = 0;
        float legTipPosY = legLength + legOfs;
        float legTipPosZ = 0;
        
        int legTipThick = legThick - 2;
        int legTipLength = legLength + 2;
        
        float legTipOfs = -(legTipThick / 2f);
        
        legTip = new ModelRenderer(this, "legtip");
        legTip.setRotationPoint(legTipPosX, legTipPosY, legTipPosZ);
        legTip.addBox("main", legTipOfs, legTipOfs, legTipOfs, legTipThick, legTipLength, legTipThick);
        leg.addChild(legTip);
        
        // foot variables
        float footPosX = 0;
        float footPosY = legTipLength + (legTipOfs / 2f);
        float footPosZ = 0;
        
        int footWidth = 10;
        int footHeight = 6;
        int footLength = 18;
        
        float footOfsX = -(footWidth / 2f);
        float footOfsY = -(footHeight / 2f);
        float footOfsZ = -12;

        foot = new ModelRenderer(this, "foot");
        foot.setRotationPoint(footPosX, footPosY, footPosZ);
        foot.addBox("main", footOfsX, footOfsY, footOfsZ, footWidth, footHeight, footLength);
        legTip.addChild(foot);
        
        // initialize animation proxies
        for (int i = 0; i < neckProxy.length; i++) {
            neckProxy[i] = new ModelPartProxy(neck);
        }
        for (int i = 0; i < tailProxy.length; i++) {
            tailProxy[i] = new ModelPartProxy(neck);
        }
        for (int i = 0; i < 4; i++) {
            legProxy[i] = new ModelPartProxy(leg);
            legTipProxy[i] = new ModelPartProxy(legTip);
            footProxy[i] = new ModelPartProxy(foot);
        }
    }
    
    private ModelRenderer buildWingFinger(boolean small) {
        ModelRenderer wingFinger = new ModelRenderer(this, "wingfinger");
        wingFinger.setRotationPoint(-47, 0, 0);
        wingFinger.addBox("bone", -70,  -1,  -1, 70,  2,  2);
        if (small) {
            wingFinger.addBox("shortskin", -70,   0,   1, 70,  0, 32);
        } else {
            wingFinger.addBox("skin", -70,   0,   1, 70,  0, 48);
        }
        wingForearm.addChild(wingFinger);
        
        return wingFinger;
    }

    @Override
    public void setLivingAnimations(EntityLiving entity, float f, float f1, float partialTicks) {
        Dragon dragon = (Dragon) entity;

        animTime = dragon.getAnimTime(partialTicks);
        groundTime = dragon.getGroundTime(partialTicks);
        flutterTime = dragon.getFlutterTime(partialTicks);
        
        cycle = (float) (animTime * Math.PI * 2);
        cycleOfs = MathF.sinL(cycle - 1) + 1;
        cycleOfs = (cycleOfs * cycleOfs + cycleOfs * 2) * 0.05f;

        flutter = dragon.isFluttering();
        back.isHidden = dragon.isSaddled();
        ground = dragon.isOnGround();
        mouthOpen = dragon.isFireBreathing();
        
        if (ground) {
            // reduce up/down amplitude
            cycleOfs *= 0.5f;
        }

        // update mounted offset
        if (dragon instanceof RidableVolantDragon) {
            RidableVolantDragon ridableDragon = (RidableVolantDragon) dragon;
            ridableDragon.yMountedOffset = 2.7f;

            if (!ground) {
                ridableDragon.yMountedOffset -= cycleOfs;
            }
        }
        
        animHeadAndNeck(dragon, partialTicks);
        animTail(dragon, partialTicks);
        animWings(dragon, partialTicks);
        animLegs(dragon, partialTicks);
    }

    @Override
    public void render(Entity entity, float f1, float f2, float f3, float f4, float f5, float scale) {
        GL11.glPushMatrix();

        float yOfs = 1;
        float zOfs = -0.5f;
        
        if (ground) {
            GL11.glTranslatef(0, yOfs, zOfs);
        } else {
            GL11.glTranslatef(0, cycleOfs + yOfs, zOfs);
        }
        
        if (renderPass == 0) {
            renderBody(scale);
        } else if (renderPass == 1) {
            renderHead(scale);
        } else {
            renderBody(scale);
            renderNeck(scale);
            renderHead(scale);
            renderTail(scale);
            renderWings(scale);
            renderLegs(scale);
        }

        GL11.glPopMatrix();
    }
    
    protected void animHeadAndNeck(Dragon dragon, float partialTicks) {
        neck.rotationPointY = 12;
        neck.rotationPointZ = -12;
        neck.rotationPointX = 0;
        
        neck.rotateAngleX = 0;
        neck.rotateAngleY = 0;
        neck.rotateAngleZ = 0;
        
        double trail[] = dragon.getTrail(VERTS_NECK + 1, partialTicks);
        double trailNeck[];
        
        for (int i = 0; i < neckProxy.length; i++) {
            trailNeck = dragon.getTrail(VERTS_NECK - i, partialTicks);
            
            neck.rotateAngleX = MathF.toRadians(MathF.normAngles(trailNeck[1] - trail[1])) * 3;
            neck.rotateAngleY = MathF.toRadians(MathF.normAngles(trailNeck[0] - trail[0])) * 2;
            
            float baseRotX = MathF.cosL((float) i * 0.45f + cycle) * 0.15f;
            
            if (ground) {
                baseRotX *= 0.2f;
            } else if (!flutter) {
                baseRotX *= 0.25f;
            }

            neck.rotateAngleX += baseRotX;
            
            if (groundTime != 0) {
                neck.rotateAngleX -= MathF.sinL(((i + 1) / (float) VERTS_NECK) * MathF.PI * 0.9f) * (groundTime * 0.5f);
            }

            neckProxy[i].renderScale = 1f;
            neckProxy[i].update();
            
            float neckSize = NECK_SIZE * neckProxy[i].renderScale - 0.7f;
            
            neck.rotationPointX -= MathF.sinL(neck.rotateAngleY) * MathF.cosL(neck.rotateAngleX) * neckSize;
            neck.rotationPointY += MathF.sinL(neck.rotateAngleX) * neckSize;
            neck.rotationPointZ -= MathF.cosL(neck.rotateAngleY) * MathF.cosL(neck.rotateAngleX) * neckSize;
        }
        
//        head.rotateAngleX = neck.rotateAngleX * 0.4f;
        head.rotateAngleX = 0;
        head.rotateAngleY = neck.rotateAngleY;
        head.rotateAngleZ = neck.rotateAngleZ * 0.2f;
        
        head.rotationPointX = neck.rotationPointX;
        head.rotationPointY = neck.rotationPointY;
        head.rotationPointZ = neck.rotationPointZ;
        
        if (mouthOpen) {
            jaw.rotateAngleX = 0.5f + MathF.sinL(cycle) * 0.05f;
        } else {
            jaw.rotateAngleX = (1 - MathF.sinL(cycle)) * 0.1f;

            if (ground) {
                jaw.rotateAngleX *= 0.5f;
            }
        }
    }
    
    protected void animWings(Dragon dragon, float partialTicks) {
        // interpolate between fluttering and gliding
        wingArm.rotateAngleX = MathF.interpCos(
                -0.25f - MathF.cosL(cycle * 4) * 0.05f * (1 - groundTime),
                0.125f - MathF.cosL(cycle) * 0.2f,
                flutterTime);
        wingArm.rotateAngleY = MathF.interpCos(
                0.25f,
                MathF.PI * 0.5f - 0.2f,
                groundTime);
        wingArm.rotateAngleZ = MathF.interpCos(
                0.35f + MathF.sinL(cycle) * 0.05f,
                (MathF.sinL(cycle) + 0.125f) * 0.8f,
                flutterTime);
        
        wingForearm.rotateAngleY = -wingArm.rotateAngleY * 2;
        wingForearm.rotateAngleZ = MathF.interpCos(
                -0.25f + (MathF.sinL(cycle + 2) + 0.5f) * 0.05f,
                -(MathF.sinL(cycle + 2) + 0.5f) * 0.75f,
                flutterTime);
        wingForearm.rotateAngleZ = MathF.interpCos(wingForearm.rotateAngleZ, 0, groundTime);
        
        // reduce Z-fighting
        wingFinger1.rotateAngleX = 0.005f;
        wingFinger2.rotateAngleX = 0.01f;
        wingFinger3.rotateAngleX = 0.015f;
        wingFinger4.rotateAngleX = 0.02f;

        // interpolate between folded and unfolded wing angles
        wingFinger1.rotateAngleY = MathF.interpCos(WING_UNFOLD[0], WING_FOLD[0], groundTime);
        wingFinger2.rotateAngleY = MathF.interpCos(WING_UNFOLD[1], WING_FOLD[1], groundTime);
        wingFinger3.rotateAngleY = MathF.interpCos(WING_UNFOLD[2], WING_FOLD[2], groundTime);
        wingFinger4.rotateAngleY = MathF.interpCos(WING_UNFOLD[3], WING_FOLD[3], groundTime);
    }
    
    protected void animTail(Dragon dragon, float partialTicks) {
        neck.rotationPointY = 16;
        neck.rotationPointZ = 62;
        neck.rotationPointX = 0;
        
        neck.rotateAngleX = 0;
        neck.rotateAngleY = 0;
        neck.rotateAngleZ = 0;
        
        float baseRotX = 0;
        
        double[] trail = dragon.getTrail(VERTS_TAIL + 1, partialTicks);
        double[] trailTail;
        
        for (int i = 0; i < tailProxy.length; i++) {      
            trailTail = dragon.getTrail(VERTS_TAIL - i, partialTicks);
            
            neck.rotateAngleY = MathF.toRadians(180);
            
            if (ground) {
                float amp = 0.1f + i / (VERTS_TAIL * 2f);
                baseRotX = (baseRotX + MathF.sinL(i * 0.45f + cycle * 0.7f)) * amp;
                neck.rotateAngleY += baseRotX * 0.6f;
                neck.rotateAngleX = (i - 6) * -amp * 0.3f;
            } else {
                baseRotX = (baseRotX + MathF.sinL(i * 0.45f + cycle) * 0.05f);
                neck.rotateAngleX = baseRotX;
            }
            
            neck.rotateAngleX += MathF.toRadians(MathF.normAngles(trailTail[1] - trail[1])) * 3;
            neck.rotateAngleY += MathF.toRadians(MathF.normAngles(trail[0] - trailTail[0]));
            
            tailProxy[i].update();
            tailProxy[i].renderScale = 1.5f - ((i + 1) / (float)VERTS_TAIL) * 1.25f;
            
            float tailSize = NECK_SIZE * tailProxy[i].renderScale - 0.7f;
            
            neck.rotationPointY += MathF.sinL(neck.rotateAngleX) * tailSize;
            neck.rotationPointZ -= MathF.cosL(neck.rotateAngleY) * MathF.cosL(neck.rotateAngleX) * tailSize;
            neck.rotationPointX -= MathF.sinL(neck.rotateAngleY) * MathF.cosL(neck.rotateAngleX) * tailSize;
        }
    }
    
    protected void animLegs(Dragon dragon, float partialTicks) {      
        float legGroundX, legGroundY, legTipGroundX, footGroundX;

        legGroundY = 0;
        footGroundX = 0.75f + cycleOfs * 0.1f;
        
        // 0 - front leg, right side
        // 1 - hind leg, right side
        // 2 - front leg, left side
        // 3 - hind leg, left side
        for (int i = 0; i < legProxy.length; i++) {
            if (i % 2 == 0) {
                leg.rotationPointZ = 1.5f;
                leg.rotateAngleY = -0.3f;
                
                if (i > 1) {
                    leg.rotateAngleX = 0.6f;
                } else {
                    leg.rotateAngleX = 0.7f;
                }
                
                legTip.rotateAngleX = -1.1f;
                
                legGroundX = 1.3f + cycleOfs * 0.1f;
                legTipGroundX = -0.5f - cycleOfs * 0.1f;
            } else {
                leg.rotationPointZ = 46;
                leg.rotateAngleY = 0.2f;
                
                if (i > 1) {
                    leg.rotateAngleX = -0.6f;
                } else {
                    leg.rotateAngleX = -0.7f;
                }
                
                legTip.rotateAngleX = 1.1f;
                
                legGroundX = 1.0f + cycleOfs * 0.1f;
                legTipGroundX = 0.5f + cycleOfs * 0.1f;
            }
            
            foot.rotateAngleX = -(leg.rotateAngleX + legTip.rotateAngleX);
            foot.rotateAngleY = -leg.rotateAngleY * 0.7f;
            foot.rotateAngleZ = 0.1f;

            // interpolate between hanging and grounded legs
            leg.rotateAngleX = MathF.interpCos(legGroundX, leg.rotateAngleX, groundTime);
            leg.rotateAngleY = MathF.interpCos(legGroundY, leg.rotateAngleY, groundTime);
            legTip.rotateAngleX = MathF.interpCos(legTipGroundX, legTip.rotateAngleX, groundTime);
            foot.rotateAngleX = MathF.interpCos(footGroundX, foot.rotateAngleX, groundTime);
            
            legProxy[i].update();
            legTipProxy[i].update();
            footProxy[i].update();
        }
    }
    
    protected void renderBody(float scale) {
        body.render(scale);
    }

    protected void renderHead(float scale) {
        head.render(scale);
    }
    
    protected void renderNeck(float scale) {
        for (ModelPartProxy proxy : neckProxy) {
            proxy.render(scale);
        }
    }

    protected void renderTail(float scale) {
        for (ModelPartProxy proxy : tailProxy) {
            proxy.render(scale);
        }
    }
    
    protected void renderWings(float scale) {     
        GL11.glEnable(GL11.GL_CULL_FACE);
        
        for (int i = 0; i < 2; i++) {
            wingArm.render(scale);

            if (i == 0) {
                // mirror next wing
                GL11.glScalef(-1, 1, 1);
                // switch to front face culling
                GL11.glCullFace(GL11.GL_FRONT);
            }
        }

        GL11.glCullFace(GL11.GL_BACK);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
    
    protected void renderLegs(float scale) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT);
        
        for (int i = 0; i < legProxy.length; i++) {
            legProxy[i].render(scale);
            
            if (i == 1) {
                // mirror next legs
                GL11.glScalef(-1, 1, 1);
                // switch to front face culling
                GL11.glCullFace(GL11.GL_BACK);
            }
        }

        
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
}
