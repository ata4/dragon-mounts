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
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonHeadPositionHelper;
import info.ata4.minecraft.dragon.server.entity.helper.SegmentSizePositionRotation;
import info.ata4.minecraft.dragon.server.entity.helper.breath.DragonBreathHelper;
import info.ata4.minecraft.dragon.server.util.DebugFreezeAnimator;
import info.ata4.minecraft.dragon.util.math.MathX;
import info.ata4.minecraft.dragon.util.math.Spline;
import net.minecraft.util.Vec3;

/**
 * Animation control class to put useless reptiles in motion. Refactored to
 * remove all client-side-only model code into DragonModel. This was necessary
 * to allow the server to calculate head positions for spawning the breath
 * weapons
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonAnimatorCommon {

    // entity parameters
    private final EntityTameableDragon entity;
    private float partialTicks;
    private float ticksExisted;
    private float moveTime;
    private float moveSpeed;
    private float netLookYaw;  //yaw of the head relative to the body
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
    private float bite;
    private float breath;
    private float speed;

    // timing interp vars
    private TickFloat animTimer = new TickFloat();
    private TickFloat groundTimer = new TickFloat(1).setLimit(0, 1);
    private TickFloat flutterTimer = new TickFloat().setLimit(0, 1);
    private TickFloat walkTimer = new TickFloat().setLimit(0, 1);
    private TickFloat sitTimer = new TickFloat().setLimit(0, 1);
    private TickFloat biteTimer = new TickFloat().setLimit(0, 1);
    private TickFloat breathTimer = new TickFloat().setLimit(0, 1);
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

    private float jawRotateAngleX;
    private float[] wingFingerRotateX;
    private float[] wingFingerRotateY;
    
    private float wingArmRotateAngleX;
    private float wingArmRotateAngleY;
    private float wingArmRotateAngleZ;
    private float wingArmPreRotateAngleX;
    private float wingForearmRotateAngleX;
    private float wingForearmRotateAngleY;
    private float wingForearmRotateAngleZ;

    private final int WING_FINGERS;
    private final int NECK_SEGMENTS;
    private final int TAIL_SEGMENTS;

    private final DragonHeadPositionHelper dragonHeadPositionHelper;

    private boolean haveCalculatedAnimations = false;
    
    private SegmentSizePositionRotation[] tailSegments;
    private SegmentSizePositionRotation tail = new SegmentSizePositionRotation();  //not required?  not sure.
    
    public DragonAnimatorCommon(EntityTameableDragon dragon) {
        this.entity = dragon;
        WING_FINGERS = dragon.getBreed().getNumberOfWingFingers();
        NECK_SEGMENTS = dragon.getBreed().getNumberOfNeckSegments();
        TAIL_SEGMENTS = dragon.getBreed().getNumberOfTailSegments();

        wingFingerRotateX = new float[WING_FINGERS];
        wingFingerRotateY = new float[WING_FINGERS];
        tailSegments = new SegmentSizePositionRotation[TAIL_SEGMENTS];
        dragonHeadPositionHelper = new DragonHeadPositionHelper(dragon, NECK_SEGMENTS);
    }
    
    public float getSpeed() {
        return speed;
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

    public float getSitTime() {
        return sit;
    }

    public float getMoveTime() {
        return moveTime;
    }

    public float getCycleOfs() {
        return cycleOfs;
    }

    public float getWingFingerRotateX(int index) {
        return wingFingerRotateX[index];
    }

    public float getWingFingerRotateY(int index) {
        return wingFingerRotateY[index];
    }

    public float getWingArmRotateAngleX() {
        return wingArmRotateAngleX;
    }

    public float getWingArmRotateAngleY() {
        return wingArmRotateAngleY;
    }

    public float getWingArmRotateAngleZ() {
        return wingArmRotateAngleZ;
    }

    public float getWingArmPreRotateAngleX() {
        return wingArmPreRotateAngleX;
    }

    public float getWingForearmRotateAngleX() {
        return wingForearmRotateAngleX;
    }

    public float getWingForearmRotateAngleY() {
        return wingForearmRotateAngleY;
    }

    public float getWingForearmRotateAngleZ() {
        return wingForearmRotateAngleZ;
    }

    public DragonHeadPositionHelper getDragonHeadPositionHelper() {
        return dragonHeadPositionHelper;
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

    public void setLook(float netLookYaw, float lookPitch) {
        // don't twist the neck
        this.netLookYaw = MathX.clamp(netLookYaw, -120, 120);
        this.lookPitch = MathX.clamp(lookPitch, -90, 90);
    }

    public Vec3 getThroatPosition() {
        if (!haveCalculatedAnimations) {
            animate();
        }
        return dragonHeadPositionHelper.getThroatPosition();
    }

    /**
     * Updates the dragon component parts - position, angles, scale. Called
     * every frame.
     */
    public void animate() {
        haveCalculatedAnimations = true;
        anim = animTimer.get(partialTicks);
        ground = groundTimer.get(partialTicks);
        flutter = flutterTimer.get(partialTicks);
        walk = walkTimer.get(partialTicks);
        sit = sitTimer.get(partialTicks);
        bite = biteTimer.get(partialTicks);
        breath = breathTimer.get(partialTicks);
        speed = speedTimer.get(partialTicks);

        animBase = anim * MathX.PI_F * 2;
        cycleOfs = MathX.sin(animBase - 1) + 1;

        // check if the wings are moving down and trigger the event
        boolean newWingsDown = cycleOfs > 1;
        if (newWingsDown && !wingsDown && flutter != 0) {
            entity.onWingsDown(speed);
        }
        wingsDown = newWingsDown;

        cycleOfs = (cycleOfs * cycleOfs + cycleOfs * 2) * 0.05f;

        // reduce up/down amplitude
        cycleOfs *= MathX.lerp(0.5f, 1, flutter);
        cycleOfs *= MathX.lerp(1, 0.5f, ground);

        // updateFromAnimator body parts
        animHeadAndNeck();
        animTail();
        animWings();
        animLegs();
    }

    /**
     * Updates the animation state. Called on every tick.
     */
    public void tickingUpdate() {
        if (DebugFreezeAnimator.isFrozen()) {
            return;
        }

        // init trails
        if (initTrails) {
            yTrail.fill((float) entity.posY);
            yawTrail.fill(entity.renderYawOffset);
            pitchTrail.fill(getBodyPitch());
            initTrails = false;
        }

        // don't move anything during death sequence
        if (entity.getHealth() <= 0) {
            animTimer.sync();
            groundTimer.sync();
            flutterTimer.sync();
            walkTimer.sync();
            sitTimer.sync();
            biteTimer.sync();
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

        // update bite opening transition and breath transitions
        DragonBreathHelper.BreathState breathState = entity.getBreathHelper().getCurrentBreathState();
        switch (breathState) {
            case IDLE: {  // breath is idle, handle bite attack
                int ticksSinceLastAttack = entity.getTicksSinceLastAttack();
                final int JAW_OPENING_TIME_FOR_ATTACK = 5;
                boolean jawFlag = (ticksSinceLastAttack >= 0 && ticksSinceLastAttack < JAW_OPENING_TIME_FOR_ATTACK);
                biteTimer.add(jawFlag ? 0.2f : -0.2f);
                breathTimer.set(0.0F);
                break;
            }
            case STARTING: {
                biteTimer.set(0.0F);
                breathTimer.set(entity.getBreathHelper().getBreathStateFractionComplete());
                break;
            }
            case STOPPING: {
                float breathStateFractionComplete = entity.getBreathHelper().getBreathStateFractionComplete();
                breathTimer.set(1.0F - breathStateFractionComplete);
                break;
            }
            case SUSTAIN: {
                breathTimer.set(1.0F);
                break;
            }
            default: {
                System.err.println("unexpected breathstate:" + breathState);
                return;
            }
        }

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

        //yTrail.update(entity.posY - entity.yOffset);
        yTrail.update(entity.posY);
        yawTrail.update(yawAbs);
        pitchTrail.update(getBodyPitch());
    }

    protected void animHeadAndNeck() {
        float bodyPitch = getBodyPitch();
        dragonHeadPositionHelper.calculateHeadAndNeck(animBase, flutter, sit, walk, speed, ground,
                netLookYaw, lookPitch, breath);
        final float BITE_ANGLE = 0.75F;
        final float BREATH_ANGLE = 0.75F;
        jawRotateAngleX = (bite * BITE_ANGLE + breath * BREATH_ANGLE);
        jawRotateAngleX += (1 - MathX.sin(animBase)) * 0.1f * flutter;
    }

    protected void animWings() {
        // move wings slower while sitting
        float aSpeed = sit > 0 ? 0.6f : 1;

        // animation speeds
        float a1 = animBase * aSpeed * 0.35f;
        float a2 = animBase * aSpeed * 0.5f;
        float a3 = animBase * aSpeed * 0.75f;

        if (ground < 1) {
            // fluttering
            wingArmFlutter[0] = 0.125f - MathX.cos(animBase) * 0.2f;
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
        wingArmRotateAngleX = wingArm[0];
        wingArmRotateAngleY = wingArm[1];
        wingArmRotateAngleZ = wingArm[2];
        wingArmPreRotateAngleX = 1 - speed;
        wingForearmRotateAngleX = wingForearm[0];
        wingForearmRotateAngleY = wingForearm[1];
        wingForearmRotateAngleZ = wingForearm[2];

        // interpolate between folded and unfolded wing angles
        float[] yFold = new float[]{2.7f, 2.8f, 2.9f, 3.0f};
        float[] yUnfold = new float[]{0.1f, 0.9f, 1.7f, 2.5f};

        // set wing finger angles
        float rotX = 0;
        float rotYOfs = MathX.sin(a1) * MathX.sin(a2) * 0.03f;
        float rotYMulti = 1;

        for (int i = 0; i < WING_FINGERS; i++) {
            wingFingerRotateX[i] = rotX += 0.005f; // reduce Z-fighting
            wingFingerRotateY[i] = MathX.slerp(yUnfold[i], yFold[i] + rotYOfs * rotYMulti, ground);
            rotYMulti -= 0.2f;
        }
    }

    public SegmentSizePositionRotation getTail() {
        return tail.getCopy();
    }

    public SegmentSizePositionRotation[] getTailSegments() {
        SegmentSizePositionRotation[] retval = new SegmentSizePositionRotation[tailSegments.length];
        for (int i = 0; i < tailSegments.length; ++i) {
            retval[i] = tailSegments[i].getCopy();
        }
        return retval;
    }

    protected void animTail() {
        tail.rotationPointX = 0;
        tail.rotationPointY = 16;
        tail.rotationPointZ = 62;
        tail.rotateAngleX = 0;
        tail.rotateAngleY = 0;
        tail.rotateAngleZ = 0;

        float rotXStand = 0;
        float rotYStand = 0;
        float rotXSit = 0;
        float rotYSit = 0;
        float rotXAir = 0;
        float rotYAir = 0;

        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            float vertMulti = (i + 1) / (float) TAIL_SEGMENTS;

            // idle
            float amp = 0.1f + i / (TAIL_SEGMENTS * 2f);

            rotXStand = (i - TAIL_SEGMENTS * 0.6f) * -amp * 0.4f;
            rotXStand += (MathX.sin(animBase * 0.2f) * MathX.sin(animBase * 0.37f) * 0.4f * amp - 0.1f) * (1 - sit);
            rotXSit = rotXStand * 0.8f;

            rotYStand = (rotYStand + MathX.sin(i * 0.45f + animBase * 0.5f)) * amp * 0.4f;
            rotYSit = MathX.sin(vertMulti * MathX.PI_F) * MathX.PI_F * 1.2f - 0.5f; // curl to the left

            rotXAir -= MathX.sin(i * 0.45f + animBase) * 0.04f * MathX.lerp(0.3f, 1, flutter);

            // interpolate between sitting and standing
            tail.rotateAngleX = MathX.lerp(rotXStand, rotXSit, sit);
            tail.rotateAngleY = MathX.lerp(rotYStand, rotYSit, sit);

            // interpolate between flying and grounded
            tail.rotateAngleX = MathX.lerp(rotXAir, tail.rotateAngleX, ground);
            tail.rotateAngleY = MathX.lerp(rotYAir, tail.rotateAngleY, ground);

            // body movement
            float angleLimit = 160 * vertMulti;
            float yawOfs = MathX
                    .clamp((float) yawTrail.getChangeInValue(partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit);
            float pitchOfs = MathX
                    .clamp((float) pitchTrail.getChangeInValue(partialTicks, 0, i + 1) * 2, -angleLimit, angleLimit);

            tail.rotateAngleX += MathX.toRadians(pitchOfs);
            tail.rotateAngleX -= (1 - speed) * vertMulti * 2;
            tail.rotateAngleY += MathX.toRadians(180 - yawOfs);

            // update scale
            float neckScale = MathX.lerp(1.5f, 0.3f, vertMulti);
            tail.setScale(neckScale);

            // update proxy
            tailSegments[i] = tail.getCopy();

            // move next proxy behind the current one
            float tailSize = DragonModel.TAIL_SIZE * tail.scaleZ - 0.7f;
            tail.rotationPointY += MathX.sin(tail.rotateAngleX) * tailSize;
            tail.rotationPointZ -= MathX.cos(tail.rotateAngleY) * MathX.cos(tail.rotateAngleX) * tailSize;
            tail.rotationPointX -= MathX.sin(tail.rotateAngleY) * MathX.cos(tail.rotateAngleX) * tailSize;
        }
    }

    protected void animLegs() {
        // do nothing - server doesn't need any of these positions so the DragonModel can do it all
    }

    static public void splineArrays(float x, boolean shift, float[] result, float[]... nodes) {
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

    static public void slerpArrays(float[] a, float[] b, float[] c, float x) {
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

    public float getBodyPitch() {
        return getBodyPitch(partialTicks);
    }

    public float getBodyPitch(float pt) {
        float pitchMovingMax = 90;
        float pitchMoving = (float) MathX.clamp(yTrail.getChangeInValue(pt, 5, 0) * 10, -pitchMovingMax, pitchMovingMax);
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

    public float getJawRotateAngleX() {
        return jawRotateAngleX;
    }
}
