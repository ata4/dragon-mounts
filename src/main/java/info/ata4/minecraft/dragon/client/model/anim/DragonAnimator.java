/*
 ** 2012 Januar 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.model.anim;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.client.model.ModelPart;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.math.MathX;
import info.ata4.minecraft.dragon.util.math.Spline;

/**
 * Animation control class to put useless reptiles in motion.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonAnimator {

    // entity parameters
    private final EntityTameableDragon entity;
    private float partialTicks;
    private float ticksExisted;
    private float moveTime;
    private float moveSpeed;
    private float lookYaw;
    private float lookPitch;
    private double prevRenderYawOffset;
    private double yawAbs;
    
    // timing vars
    private float animBase;
    private float cycleOfs;
    private float anim;
    private float ground;
    private float flutter;
    private float walk;
    private float sit;
    private float jaw;
    private float speed;
    
    // timing interp vars
    private TickFloat animTimer = new TickFloat();
    private TickFloat groundTimer = new TickFloat(1).setLimit(0, 1);
    private TickFloat flutterTimer = new TickFloat().setLimit(0, 1);
    private TickFloat walkTimer = new TickFloat().setLimit(0, 1);
    private TickFloat sitTimer = new TickFloat().setLimit(0, 1);
    private TickFloat jawTimer = new TickFloat().setLimit(0, 1);
    private TickFloat speedTimer = new TickFloat(1).setLimit(0, 1);
    
    // trails
    private boolean initTrails = true;
    private CircularBuffer yTrail = new CircularBuffer(8);
    private CircularBuffer yawTrail = new CircularBuffer(16);
    private CircularBuffer pitchTrail = new CircularBuffer(16);
    
    // model flags
    private boolean onGround;
    private boolean openJaw;
    private boolean wingsDown;
    
    // animation parameters
    private float[] wingArm = new float[3];
    private float[] wingForearm = new float[3];
    private float[] wingArmFlutter = new float[3];
    private float[] wingForearmFlutter = new float[3];
    private float[] wingArmGlide = new float[3];
    private float[] wingForearmGlide = new float[3];
    private float[] wingArmGround = new float[3];
    private float[] wingForearmGround = new float[3];
    
    // final X rotation angles for ground
    private float[] xGround = {0, 0, 0, 0};
    
    // X rotation angles for ground
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private float[][] xGroundStand = {
        {0.8f, -1.5f, 1.3f, 0},
        {-0.3f, 1.5f, -0.2f, 0},
    };
    private float[][] xGroundSit = {
        {0.3f, -1.8f, 1.8f, 0},
        {-0.8f, 1.8f, -0.9f, 0},
    };

    // X rotation angles for walking
    // 1st dim - animation keyframe
    // 2nd dim - front, hind
    // 3rd dim - thigh, crus, foot, toe
    private float[][][] xGroundWalk = {{
        {0.4f, -1.4f, 1.3f, 0},    // move down and forward
        {0.1f, 1.2f, -0.5f, 0}     // move back
    }, {
        {1.2f, -1.6f, 1.3f, 0},    // move back
        {-0.3f, 2.1f, -0.9f, 0.6f} // move up and forward
    }, {
        {0.9f, -2.1f, 1.8f, 0.6f}, // move up and forward
        {-0.7f, 1.4f, -0.2f, 0}    // move down and forward
    }};
    
    // final X rotation angles for walking
    private float[] xGroundWalk2 = {0, 0, 0, 0};
    
    // Y rotation angles for ground, thigh only
    private float[] yGroundStand = {-0.25f, 0.25f};
    private float[] yGroundSit = {0.1f, 0.35f};
    private float[] yGroundWalk = {-0.1f, 0.1f};
    
    // final X rotation angles for air
    private float[] xAir;
    
    // X rotation angles for air
    // 1st dim - front, hind
    // 2nd dim - thigh, crus, foot, toe
    private float[][] xAirAll = {{0, 0, 0, 0}, {0, 0, 0, 0}};
    
    // Y rotation angles for air, thigh only
    private float[] yAirAll = {-0.1f, 0.1f};
    
    public DragonAnimator(EntityTameableDragon dragon) {
        this.entity = dragon;
    }
    
    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
    
    public void setTicksExisted(float ticksExisted) {
        this.ticksExisted = ticksExisted;
    }

    public void setMovement(float moveTime, float moveSpeed) {
        this.moveTime = moveTime;
        this.moveSpeed = moveSpeed;
    }

    public void setLook(float lookYaw, float lookPitch) {
        // don't twist the neck
        this.lookYaw = MathX.clamp(lookYaw, -120, 120);
        this.lookPitch = MathX.clamp(lookPitch, -90, 90);
    }
    
    /**
     * Applies the animations on the model. Called every frame before the model
     * is rendered.
     * 
     * @param model model to animate
     */
    public void animate(DragonModel model) {
        anim = animTimer.get(partialTicks);
        ground = groundTimer.get(partialTicks);
        flutter = flutterTimer.get(partialTicks);
        walk = walkTimer.get(partialTicks);
        sit = sitTimer.get(partialTicks);
        jaw = jawTimer.get(partialTicks);
        speed = speedTimer.get(partialTicks);
        
        animBase = anim * MathX.PI_F * 2;
        cycleOfs = MathX.sin(animBase - 1) + 1;
        
        // check if the wings are moving down and trigger the event
        boolean newWingsDown = cycleOfs > 1;
        if (newWingsDown && !wingsDown && flutter != 0) {
            entity.onWingsDown(speed);
        }
        wingsDown = newWingsDown;
        
        // update flags
        model.back.isHidden = entity.isSaddled();
        
        cycleOfs = (cycleOfs * cycleOfs + cycleOfs * 2) * 0.05f;

        // reduce up/down amplitude
        cycleOfs *= MathX.lerp(0.5f, 1, flutter);
        cycleOfs *= MathX.lerp(1, 0.5f, ground);
        
        // update offsets
        model.offsetX = getModelOffsetX();
        model.offsetY = getModelOffsetY();
        model.offsetZ = getModelOffsetZ();
        
        // update pitch
        model.pitch = getModelPitch();
        
        // animate body parts
        animHeadAndNeck(model);
        animTail(model);
        animWings(model);
        animLegs(model);
    }
    
    /**
     * Updates the animation state. Called on every tick.
     */
    public void update() {
        // init trails
        if (initTrails) {
            yTrail.fill((float) entity.posY);
            yawTrail.fill(entity.renderYawOffset);
            pitchTrail.fill(getModelPitch());
            initTrails = false;
        }
         
        // don't move anything during death sequence
        if (entity.getHealth() <= 0) {
            animTimer.sync();
            groundTimer.sync();
            flutterTimer.sync();
            walkTimer.sync();
            sitTimer.sync();
            jawTimer.sync();
            return;
        }
        
        float speedMax = 0.05f;
        float speedEnt = (float) (entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
        float speedMulti = MathX.clamp(speedEnt / speedMax, 0, 1);
         
        // update main animation timer
        float animAdd = 0.035f;
        
        // depend timing speed on movement
        if (!onGround) {
            animAdd += (1 - speedMulti) * animAdd;
        }
        
        animTimer.add(animAdd);
        
        // update ground transition
        float groundVal = groundTimer.get();
        if (onGround) {
            groundVal *= 0.95f;
            groundVal += 0.08f;
        } else {
            groundVal -= 0.1f;
        }
        groundTimer.set(groundVal);

        // update flutter transition
        boolean flutterFlag = !onGround && (entity.isCollided || entity.motionY > -0.1 || speedEnt < speedMax);
        flutterTimer.add(flutterFlag ? 0.1f : -0.1f);

        // update walking transition
        boolean walkFlag = moveSpeed > 0.1 && !entity.isSitting();
        float walkVal = 0.1f;
        walkTimer.add(walkFlag ? walkVal : -walkVal);
        
        // update sitting transisiton
        float sitVal = sitTimer.get();
        sitVal += entity.isSitting() ? 0.1f : -0.1f;
        sitVal *= 0.95f;
        sitTimer.set(sitVal);
        
        // update jaw opening transition
        boolean jawFlag = (entity.attackTime < 20 && entity.attackTime > 15);
        jawTimer.add(jawFlag ? 0.2f : -0.2f);

        // update speed transition
        boolean nearGround = entity.getAltitude() < entity.height * 2;
        boolean speedFlag = speedEnt > speedMax || onGround || nearGround;
        float speedValue = 0.05f;
        speedTimer.add(speedFlag ? speedValue : -speedValue);
           
        // update trailers
        double yawDiff = entity.renderYawOffset - prevRenderYawOffset;
        prevRenderYawOffset = entity.renderYawOffset;
        
        // filter out 360 degrees wrapping
        if (yawDiff < 180 && yawDiff > -180) {
            yawAbs += yawDiff;
        }

        yTrail.update(entity.posY - entity.yOffset);
        yawTrail.update(yawAbs);
        pitchTrail.update(getModelPitch());
    }
        
    public float getAnimTime() {
        return anim;
    }
    
    public float getGroundTime() {
        return ground;
    }
    
    public float getFlutterTime() {
        return flutter;
    }
    
    public float getWalkTime() {
        return walk;
    }
    
    protected void animHeadAndNeck(DragonModel model) {
        model.neck.rotationPointX = 0;
        model.neck.rotationPointY = 14;
        model.neck.rotationPointZ = -8;
        
        model.neck.rotateAngleX = 0;
        model.neck.rotateAngleY = 0;
        model.neck.rotateAngleZ = 0;
        
        double health = entity.getHealthRelative();
        float neckSize;

        for (int i = 0; i < model.neckProxy.length; i++) {
            float vertMulti = (i + 1) / (float) model.neckProxy.length;

            float baseRotX = MathX.cos((float) i * 0.45f + animBase) * 0.15f;
            baseRotX *= MathX.lerp(0.2f, 1, flutter);
            baseRotX *= MathX.lerp(1, 0.2f, sit);
            float ofsRotX = MathX.sin(vertMulti * MathX.PI_F * 0.9f) * 0.75f;
            
            // basic up/down movement
            model.neck.rotateAngleX = baseRotX;
            // reduce rotation when on ground
            model.neck.rotateAngleX *= MathX.slerp(1, 0.5f, walk);
            // flex neck down when hovering
            model.neck.rotateAngleX += (1 - speed) * vertMulti;
            // lower neck on low health
            model.neck.rotateAngleX -= MathX.lerp(0, ofsRotX, ground * health);
            // use looking yaw
            model.neck.rotateAngleY = MathX.toRadians(lookYaw) * vertMulti * speed;
            
            // update scale
            model.neck.renderScaleX = model.neck.renderScaleY = MathX.lerp(1.6f, 1, vertMulti);
            model.neck.renderScaleZ = 0.6f;
            
            // hide the first and every second scale
            model.neckScale.isHidden = i % 2 != 0 || i == 0;
            
            // update proxy
            model.neckProxy[i].update();
            
            // move next proxy behind the current one
            neckSize = DragonModel.NECK_SIZE * model.neck.renderScaleZ - 1.4f;
            model.neck.rotationPointX -= MathX.sin(model.neck.rotateAngleY) * MathX.cos(model.neck.rotateAngleX) * neckSize;
            model.neck.rotationPointY += MathX.sin(model.neck.rotateAngleX) * neckSize;
            model.neck.rotationPointZ -= MathX.cos(model.neck.rotateAngleY) * MathX.cos(model.neck.rotateAngleX) * neckSize;
        }
        
        model.head.rotateAngleX = MathX.toRadians(lookPitch) + (1 - speed);
        model.head.rotateAngleY = model.neck.rotateAngleY;
        model.head.rotateAngleZ = model.neck.rotateAngleZ * 0.2f;
        
        model.head.rotationPointX = model.neck.rotationPointX;
        model.head.rotationPointY = model.neck.rotationPointY;
        model.head.rotationPointZ = model.neck.rotationPointZ;
        
        model.jaw.rotateAngleX = jaw * 0.75f;
        model.jaw.rotateAngleX += (1 - MathX.sin(animBase)) * 0.1f * flutter;
    }
    
    protected void animWings(DragonModel model) {
        // move wings slower while sitting
        float aSpeed = sit > 0 ? 0.6f : 1;
        
        // animation speeds
        float a1 = animBase * aSpeed * 0.35f;
        float a2 = animBase * aSpeed * 0.5f;
        float a3 = animBase * aSpeed * 0.75f;
        
        if (ground < 1) {
            // fluttering
            wingArmFlutter[0] = 0.125f - MathX.cos(animBase) * 0.2f ;
            wingArmFlutter[1] = 0.25f;
            wingArmFlutter[2] = (MathX.sin(animBase) + 0.125f) * 0.8f;
            
            wingForearmFlutter[0] = 0;
            wingForearmFlutter[1] = -wingArmFlutter[1] * 2;
            wingForearmFlutter[2] = -(MathX.sin(animBase + 2) + 0.5f) * 0.75f;

            // gliding
            wingArmGlide[0] = -0.25f - MathX.cos(animBase * 2) * MathX.cos(animBase * 1.5f) * 0.04f;
            wingArmGlide[1] = 0.25f;
            wingArmGlide[2] = 0.35f + MathX.sin(animBase) * 0.05f;

            wingForearmGlide[0] = 0;
            wingForearmGlide[1] = -wingArmGlide[1] * 2;
            wingForearmGlide[2] = -0.25f + (MathX.sin(animBase + 2) + 0.5f) * 0.05f;
        }
        
        if (ground > 0) {
            // standing
            wingArmGround[0] = 0;
            wingArmGround[1] = 1.4f - MathX.sin(a1) * MathX.sin(a2) * 0.02f;
            wingArmGround[2] = 0.8f + MathX.sin(a2) * MathX.sin(a3) * 0.05f;

            // walking
            wingArmGround[1] += MathX.sin(moveTime * 0.5f) * 0.02f * walk;
            wingArmGround[2] += MathX.cos(moveTime * 0.5f) * 0.05f * walk;
            
            wingForearmGround[0] = 0;
            wingForearmGround[1] = -wingArmGround[1] * 2;
            wingForearmGround[2] = 0;
        }
        
        // interpolate between fluttering and gliding
        slerpArrays(wingArmGlide, wingArmFlutter, wingArm, flutter);
        slerpArrays(wingForearmGlide, wingForearmFlutter, wingForearm, flutter);
        
        // interpolate between flying and grounded
        slerpArrays(wingArm, wingArmGround, wingArm, ground);
        slerpArrays(wingForearm, wingForearmGround, wingForearm, ground);
        
        // apply angles
        model.wingArm.rotateAngleX = wingArm[0];
        model.wingArm.rotateAngleY = wingArm[1];
        model.wingArm.rotateAngleZ = wingArm[2];
        model.wingArm.preRotateAngleX = 1 - speed;
        model.wingForearm.rotateAngleX = wingForearm[0];
        model.wingForearm.rotateAngleY = wingForearm[1];
        model.wingForearm.rotateAngleZ = wingForearm[2];
        
        // interpolate between folded and unfolded wing angles
        float[] yFold = new float[] {2.7f, 2.8f, 2.9f, 3.0f};
        float[] yUnfold = new float[] {0.1f, 0.9f, 1.7f, 2.5f};
        
        // set wing finger angles
        float rotX = 0;
        float rotYOfs = MathX.sin(a1) * MathX.sin(a2) * 0.03f;
        float rotYMulti = 1;
        
        for (int i = 0; i < model.wingFinger.length; i++) {
            model.wingFinger[i].rotateAngleX = rotX += 0.005f; // reduce Z-fighting
            model.wingFinger[i].rotateAngleY = MathX.slerp(yUnfold[i],
                    yFold[i] + rotYOfs * rotYMulti, ground);
            rotYMulti -= 0.2f;
        }
    }
    
    protected void animTail(DragonModel model) {
        model.tail.rotationPointX = 0;
        model.tail.rotationPointY = 16;
        model.tail.rotationPointZ = 62;
        
        model.tail.rotateAngleX = 0;
        model.tail.rotateAngleY = 0;
        model.tail.rotateAngleZ = 0;
        
        float rotXStand = 0;
        float rotYStand = 0;
        float rotXSit = 0;
        float rotYSit = 0;
        float rotXAir = 0;
        float rotYAir = 0;
        
        for (int i = 0; i < model.tailProxy.length; i++) {
            float vertMulti = (i + 1) / (float) model.tailProxy.length;

            // idle
            float amp = 0.1f + i / (model.tailProxy.length * 2f);
            
            rotXStand = (i - model.tailProxy.length * 0.6f) * -amp * 0.4f;
            rotXStand += (MathX.sin(animBase * 0.2f) * MathX.sin(animBase * 0.37f) * 0.4f * amp - 0.1f) * (1 - sit);
            rotXSit = rotXStand * 0.8f;

            rotYStand = (rotYStand + MathX.sin(i * 0.45f + animBase * 0.5f)) * amp * 0.4f;
            rotYSit = MathX.sin(vertMulti * MathX.PI_F) * MathX.PI_F * 1.2f - 0.5f; // curl to the left
            
            rotXAir -= MathX.sin(i * 0.45f + animBase) * 0.04f * MathX.lerp(0.3f, 1, flutter);
            
            // interpolate between sitting and standing
            model.tail.rotateAngleX = MathX.lerp(rotXStand, rotXSit, sit);
            model.tail.rotateAngleY = MathX.lerp(rotYStand, rotYSit, sit);
            
            // interpolate between flying and grounded
            model.tail.rotateAngleX = MathX.lerp(rotXAir, model.tail.rotateAngleX, ground);
            model.tail.rotateAngleY = MathX.lerp(rotYAir, model.tail.rotateAngleY, ground);
            
            // body movement
            float angleLimit = 160 * vertMulti;
            float yawOfs = MathX.clamp((float) yawTrail.get(partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit);
            float pitchOfs = MathX.clamp((float) pitchTrail.get(partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit);
            
            model.tail.rotateAngleX += MathX.toRadians(pitchOfs);
            model.tail.rotateAngleX -= (1 - speed) * vertMulti * 2;
            model.tail.rotateAngleY += MathX.toRadians(180 - yawOfs);
            
            // display horns near the tip
            boolean horn = i > model.tailProxy.length - 7 && i < model.tailProxy.length - 3;
            model.tailHornLeft.isHidden = model.tailHornRight.isHidden = !horn;

            // update scale
            float neckScale = MathX.lerp(1.5f, 0.3f, vertMulti);
            model.tail.setRenderScale(neckScale);
            
            // update proxy
            model.tailProxy[i].update();
            
            // move next proxy behind the current one
            float tailSize = DragonModel.TAIL_SIZE * model.tail.renderScaleZ - 0.7f;
            model.tail.rotationPointY += MathX.sin(model.tail.rotateAngleX) * tailSize;
            model.tail.rotationPointZ -= MathX.cos(model.tail.rotateAngleY) * MathX.cos(model.tail.rotateAngleX) * tailSize;
            model.tail.rotationPointX -= MathX.sin(model.tail.rotateAngleY) * MathX.cos(model.tail.rotateAngleX) * tailSize;
        }
    }
    
    protected void animLegs(DragonModel model) {
        // dangling legs for flying
        if (ground < 1) {
            float footAirOfs = cycleOfs * 0.1f;
            float footAirX = 0.75f + cycleOfs * 0.1f;

            xAirAll[0][0] = 1.3f + footAirOfs;
            xAirAll[0][1] = -(0.7f * speed + 0.1f + footAirOfs);
            xAirAll[0][2] = footAirX;
            xAirAll[0][3] = footAirX * 0.5f;

            xAirAll[1][0] = footAirOfs + 0.6f;
            xAirAll[1][1] = footAirOfs + 0.8f;
            xAirAll[1][2] = footAirX;
            xAirAll[1][3] = footAirX * 0.5f;
        }
        
        // 0 - front leg, right side
        // 1 - hind leg, right side
        // 2 - front leg, left side
        // 3 - hind leg, left side
        for (int i = 0; i < model.thighProxy.length; i++) {
            ModelPart thigh, crus, foot, toe;
            
            if (i % 2 == 0) {
                thigh = model.forethigh;
                crus = model.forecrus;
                foot = model.forefoot;
                toe = model.foretoe;
                
                thigh.rotationPointZ = 4;
            } else {
                thigh = model.hindthigh;
                crus = model.hindcrus;
                foot = model.hindfoot; 
                toe = model.hindtoe;
                
                thigh.rotationPointZ = 46;
            }
            
            xAir = xAirAll[i % 2];
            
            // interpolate between sitting and standing
            slerpArrays(xGroundStand[i % 2], xGroundSit[i % 2], xGround, sit);
            
            // align the toes so they're always horizontal on the ground
            xGround[3] = -(xGround[0] + xGround[1] + xGround[2]);
            
            // apply walking cycle
            if (walk > 0) {
                // interpolate between the keyframes, based on the cycle
                splineArrays(moveTime * 0.2f, i > 1, xGroundWalk2,
                        xGroundWalk[0][i % 2], xGroundWalk[1][i % 2], xGroundWalk[2][i % 2]);
                // align the toes so they're always horizontal on the ground
                xGroundWalk2[3] -= xGroundWalk2[0] + xGroundWalk2[1] + xGroundWalk2[2];
                
                slerpArrays(xGround, xGroundWalk2, xGround, walk);
            }
            
            float yAir = yAirAll[i % 2];
            float yGround;
            
            // interpolate between sitting and standing
            yGround = MathX.slerp(yGroundStand[i % 2], yGroundSit[i % 2], sit);
            
            // interpolate between standing and walking
            yGround = MathX.slerp(yGround, yGroundWalk[i % 2], walk);
            
            // interpolate between flying and grounded
            thigh.rotateAngleY = MathX.slerp(yAir, yGround, ground);
            thigh.rotateAngleX = MathX.slerp(xAir[0], xGround[0], ground);
            crus.rotateAngleX = MathX.slerp(xAir[1], xGround[1], ground);
            foot.rotateAngleX = MathX.slerp(xAir[2], xGround[2], ground);
            toe.rotateAngleX = MathX.slerp(xAir[3], xGround[3], ground);
            
            // update proxy
            model.thighProxy[i].update();
        }
    }
    
    private void splineArrays(float x, boolean shift, float[] result, float[]... nodes) {
        // uncomment to disable interpolation
//        if (true) {
//            if (shift) {
//                System.arraycopy(nodes[(int) (x + 1) % nodes.length], 0, result, 0, nodes.length);
//            } else {
//                System.arraycopy(nodes[(int) x % nodes.length], 0, result, 0, nodes.length);
//            }
//            return;
//        }
        
        int i1 = (int) x % nodes.length;
        int i2 = (i1 + 1) % nodes.length;
        int i3 = (i1 + 2) % nodes.length;
        
        float[] a1 = nodes[i1];
        float[] a2 = nodes[i2];
        float[] a3 = nodes[i3];
        
        float xn = x % nodes.length - i1;
        
        if (shift) {
            Spline.interp(xn, result, a2, a3, a1, a2);
        } else {
            Spline.interp(xn, result, a1, a2, a3, a1);
        }
    }
    
    private void slerpArrays(float[] a, float[] b, float[] c, float x) {
        if (a.length != b.length || b.length != c.length) {
            throw new IllegalArgumentException();
        }
        
        if (x <= 0) {
            System.arraycopy(a, 0, c, 0, a.length);
            return;
        }
        if (x >= 1) {
            System.arraycopy(b, 0, c, 0, a.length);
            return;
        }

        for (int i = 0; i < c.length; i++) {
            c[i] = MathX.slerp(a[i], b[i], x);
        }
    }
    
    public float getModelPitch() {
        return getModelPitch(partialTicks);
    }
    
    public float getModelPitch(float pt) {
        float pitchMovingMax = 90;
        float pitchMoving = (float) MathX.clamp(yTrail.get(pt, 5, 0) * 10, -pitchMovingMax, pitchMovingMax);
        float pitchHover = 60;
        return MathX.slerp(pitchHover, pitchMoving, speed);
    }
    
    public float getModelOffsetX() {
        return 0;
    }
    
    public float getModelOffsetY() {
        return -1.5f + (sit * 0.6f);
    }
    
    public float getModelOffsetZ() {
        return -1.5f;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isOpenJaw() {
        return openJaw;
    }

    public void setOpenJaw(boolean openJaw) {
        this.openJaw = openJaw;
    }
}
