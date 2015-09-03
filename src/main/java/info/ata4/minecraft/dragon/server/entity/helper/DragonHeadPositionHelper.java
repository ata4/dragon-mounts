package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
* Created by TGG on 24/06/2015.
 *
 * Usage:
 * 1) Caller should use setHeadLocation to provide details of where the head is rotated relative to the body
 * 2) Caller should use setBodyPitch to tell the pitch of the dragon's body
 * 3) call getThroatPosition() to get the [x,y,z] position of the throat.
*/
public class DragonHeadPositionHelper
{
  public DragonHeadPositionHelper(EntityTameableDragon parent, int i_numberOfNeckSegments)
  {
    dragon = parent;
    NUMBER_OF_NECK_SEGMENTS = i_numberOfNeckSegments;
  }

  /** calculate the position, rotation angles, and scale of the head and all segments in the neck
   * @param animBase
   * @param flutter
   * @param sit
   * @param walk
   * @param speed
   * @param ground
   * @param netLookYaw
   * @param lookPitch
   * @param bodyPitch
   * @param breath
   */
  public void calculateHeadAndNeck(float animBase, float flutter, float sit, float walk, float speed, float ground,
                                   float netLookYaw, float lookPitch, float bodyPitch, float breath)
  {
//    System.out.format("%s bodyPitch=%.0f\n", (dragon.worldObj.isRemote ? "client:" : "server:                "), bodyPitch);  // todo remove
    neckSegments = new SegmentSizePositionRotation[NUMBER_OF_NECK_SEGMENTS];
    head = new SegmentSizePositionRotation();
    SegmentSizePositionRotation currentSegment = new SegmentSizePositionRotation();

    currentSegment.rotationPointX = 0;
    currentSegment.rotationPointY = 14;
    currentSegment.rotationPointZ = -8;
    currentSegment.rotateAngleX = 0;
    currentSegment.rotateAngleY = 0;
    currentSegment.rotateAngleZ = 0;

    double health = dragon.getHealthRelative();
    for (int i = 0; i < NUMBER_OF_NECK_SEGMENTS; i++) {
      float vertMulti = (i + 1) / (float)NUMBER_OF_NECK_SEGMENTS;

      float baseRotX = MathX.cos((float) i * 0.45f + animBase) * 0.15f;
      baseRotX *= MathX.lerp(0.2f, 1, flutter);
      baseRotX *= MathX.lerp(1, 0.2f, sit);
      float ofsRotX = MathX.sin(vertMulti * MathX.PI_F * 0.9f) * 0.75f;

      // basic up/down movement
      currentSegment.rotateAngleX = baseRotX;
      // reduce rotation when on ground
      currentSegment.rotateAngleX *= MathX.slerp(1, 0.5f, walk);
      // flex neck down when hovering
      currentSegment.rotateAngleX += (1 - speed) * vertMulti;
      // lower neck on low health
      currentSegment.rotateAngleX -= MathX.lerp(0, ofsRotX, ground * health);
      // use looking yaw
      currentSegment.rotateAngleY = MathX.toRadians(netLookYaw) * vertMulti * speed;

      // update size (scale)
      currentSegment.scaleX = currentSegment.scaleY = MathX.lerp(1.6f, 1, vertMulti);
      currentSegment.scaleZ = 0.6f;

       neckSegments[i] = currentSegment.getCopy();

      // move next segment behind the current one
      float neckSize = DragonModel.NECK_SIZE * currentSegment.scaleZ - 1.4f;
      currentSegment.rotationPointX -= MathX.sin(currentSegment.rotateAngleY) * MathX.cos(currentSegment.rotateAngleX) * neckSize;
      currentSegment.rotationPointY += MathX.sin(currentSegment.rotateAngleX) * neckSize;
      currentSegment.rotationPointZ -= MathX.cos(currentSegment.rotateAngleY) * MathX.cos(currentSegment.rotateAngleX) * neckSize;
    }
    neck = currentSegment.getCopy();  // might not be required, not sure, so do it anyway...

    final float HEAD_TILT_DURING_BREATH = -0.1F;
//    System.out.format("%s lookPitch=%.2f, speed=%.2f, breath=%.2f\n",
//            (dragon.worldObj.isRemote ? "client:" : "server:"), lookPitch, speed, breath);
    head.rotateAngleX = MathX.toRadians(lookPitch) + (1 - speed) + breath * HEAD_TILT_DURING_BREATH;
    head.rotateAngleY = currentSegment.rotateAngleY;
    head.rotateAngleZ = currentSegment.rotateAngleZ * 0.2f;

    head.rotationPointX = currentSegment.rotationPointX;
    head.rotationPointY = currentSegment.rotationPointY;
    head.rotationPointZ = currentSegment.rotationPointZ;
   }

  public SegmentSizePositionRotation getHeadPositionSizeLocation()
  {
    if (head == null) {
      throw new IllegalStateException("DragonHeadPositionHelper.calculateHeadAndNeck() must be called first");
    }
    return head.getCopy();
  }

  public SegmentSizePositionRotation getNeckPositionSizeLocation()
  {
    if (neck == null) {
      throw new IllegalStateException("DragonHeadPositionHelper.calculateHeadAndNeck() must be called first");
    }
    return neck.getCopy();
  }

  public SegmentSizePositionRotation[] getNeckSegmentPositionSizeLocations()
  {
    if (neckSegments == null) {
      throw new IllegalStateException("DragonHeadPositionHelper.calculateHeadAndNeck() must be called first");
    }
    SegmentSizePositionRotation[] retval = new SegmentSizePositionRotation[neckSegments.length];
    for (int i = 0; i < neckSegments.length; ++i) {
      retval[i] = neckSegments[i].getCopy();
    }
    return retval;
  }

  /** Calculate the position of the dragon's throat
   * Must have previously called calculateHeadAndNeck()
   * @return the world [x,y,z] of the throat
   */
  public Vec3 getThroatPosition()
  {
    if (head == null) {
      throw new IllegalStateException("DragonHeadPositionHelper.calculateHeadAndNeck() must be called first");
    }

//    System.out.println((dragon.worldObj.isRemote ? "client:" : "server:") + head);  // todo remove
//    float eyeHeight = dragon.getEyeHeight();
//    Vec3 posVec = dragon.getPositionVector();
//    float yaw = dragon.rotationYaw;
//    float yawHead = dragon.getRotationYawHead();
//    float getYawHead = dragon.getRotationYawHead();
//    float pitch = dragon.rotationPitch;

    float renderYawOffset = dragon.renderYawOffset;

//    System.out.println("getThroatPosition():" + (dragon.worldObj.isRemote ? "client" : "server")
//            + " eyeHeight:" + eyeHeight + " posVec:" + posVec + " yaw:" + yaw + " yawHead:" + yawHead + " getYawHead:" + getYawHead
//           + " pitch:" + pitch + " renderYawOffset:" + renderYawOffset);
//    System.out.println("headLocation:" + headLocation);

    Vec3 bodyOrigin = dragon.getPositionVector();
    bodyOrigin = bodyOrigin.addVector(0, dragon.getEyeHeight(), 0);
    float scale = dragon.getScale();
    final float BODY_X_SCALE = -0.05F * scale;
    final float BODY_Y_SCALE = -0.05F * scale;
    final float BODY_Z_SCALE = 0.05F * scale;

    final float headScale = scale * (1.4f / (scale + 0.4f));   // from DragonModel.renderHead()
    final float HEAD_X_SCALE = 0.05F * headScale;
    final float HEAD_Y_SCALE = 0.05F * headScale;
    final float HEAD_Z_SCALE = 0.05F * headScale;

    // the head offset plus the headLocation.rotationPoint is the origin of the head, i.e. the point about which the
    //   head rotates, relative to the origin of the body (getPositionEyes)
    final float HEAD_X_OFFSET = 0;
    final float HEAD_Y_OFFSET = 2;
    final float HEAD_Z_OFFSET = -23;

    final float THROAT_X_OFFSET = 0;
    final float THROAT_Y_OFFSET = -8;
    final float THROAT_Z_OFFSET = -17;

    Vec3 headOffset =  new Vec3((head.rotationPointX + HEAD_X_OFFSET) * BODY_X_SCALE,
                                (head.rotationPointY + HEAD_Y_OFFSET) * BODY_Y_SCALE,
                                (head.rotationPointZ + HEAD_Z_OFFSET) * BODY_Z_SCALE);

    // offset of the throat position relative to the head origin- rotate and pitch to match head

    Vec3 throatOffset = new Vec3(THROAT_X_OFFSET * HEAD_X_SCALE,
            THROAT_Y_OFFSET * HEAD_Y_SCALE,
            THROAT_Z_OFFSET * HEAD_Z_SCALE);

    throatOffset = throatOffset.rotatePitch(head.rotateAngleX);
    throatOffset = throatOffset.rotateYaw(-head.rotateAngleY);

//    Random random = new Random();
//    if (random.nextBoolean()) {
    Vec3 headPlusThroatOffset = headOffset.add(throatOffset);

    float bodyPitch = dragon.getBodyPitch();
//    bodyPitch = 60; // todo remove!
//
//    double y = 0;
//    double z = 0;
//    EntityPlayerSP playerSP = Minecraft.getMinecraft().thePlayer;
//    ItemStack is1 = playerSP.inventory.getStackInSlot(0);
//    ItemStack is2 = playerSP.inventory.getStackInSlot(1);
//    if (is1 != null) y = is1.stackSize;
//    if (is2 != null) z = is2.stackSize;
//    Vec3 CENTRE_OFFSET = new Vec3(0, -y * BODY_Y_SCALE,  z * BODY_Z_SCALE);
    Vec3 CENTRE_OFFSET = new Vec3(0, -6 * BODY_Y_SCALE,  19 * BODY_Z_SCALE);

    //rotate body

    bodyPitch = (float)Math.toRadians(bodyPitch);
//    String before = headPlusThroatOffset.toString();

    headPlusThroatOffset = headPlusThroatOffset.add(CENTRE_OFFSET);
    headPlusThroatOffset = headPlusThroatOffset.rotatePitch(-bodyPitch);
    headPlusThroatOffset = headPlusThroatOffset.subtract(CENTRE_OFFSET);
//    String after = headPlusThroatOffset.toString();
//    System.out.format("before=%s, after=%s\n", before, after);

    headPlusThroatOffset = headPlusThroatOffset.rotateYaw((float) (Math.toRadians(-renderYawOffset) + Math.PI));

    Vec3 throatPos = bodyOrigin.add(headPlusThroatOffset);

//    System.out.println("throatPos:" + throatPos);
    return throatPos;
  }

  /**
   * rotate a vector around the X axis
   * @param angle in radians
   * @return
   */
  public Vec3 rotateX(Vec3 source, float angle)
  {
    float cosAngle = MathHelper.cos(angle);
    float sinAngle = MathHelper.sin(angle);
    double d0 = source.xCoord;
    double d1 = source.yCoord * (double)cosAngle + source.zCoord * (double)sinAngle;
    double d2 = source.zCoord * (double)cosAngle - source.yCoord * (double)sinAngle;
    return new Vec3(d0, d1, d2);
  }

  public Vec3 rotateY(Vec3 source, float angle)
  {
    float cosAngle = MathHelper.cos(angle);
    float sinAngle = MathHelper.sin(angle);
    double d0 = source.xCoord * (double)cosAngle + source.zCoord * (double)sinAngle;
    double d1 = source.yCoord;
    double d2 = source.zCoord * (double)cosAngle - source.xCoord * (double)sinAngle;
    return new Vec3(d0, d1, d2);
  }

  public Vec3 rotateZ(Vec3 source, float angle)
  {
    float cosAngle = MathHelper.cos(angle);
    float sinAngle = MathHelper.sin(angle);
    double d0 = source.xCoord * (double)cosAngle + source.yCoord * (double)sinAngle;
    double d1 = source.yCoord * (double)cosAngle - source.xCoord * (double)sinAngle;
    double d2 = source.zCoord;
    return new Vec3(d0, d1, d2);
  }

//  public void setSegmentSizePositionRotation(SegmentSizePositionRotation segmentSizePositionRotation) {
//    this.head = segmentSizePositionRotation;
//  }

  private SegmentSizePositionRotation[] neckSegments;
  private SegmentSizePositionRotation head;
  private SegmentSizePositionRotation neck;  //not required?  not sure.

//  // taken from DragonAnimator.animHeadAndNeck
//  private HeadLocation calculateHeadPosition(EntityTameableDragon dragon) {
//    HeadLocation head = new HeadLocation();
//    head.rotationPointX = 0;
//    head.rotationPointY = 14;
//    head.rotationPointZ = -8;
//
//    head.rotateAngleX = 0;
//    head.rotateAngleY = 0;
//    head.rotateAngleZ = 0;
//
//    double health = dragon.getHealthRelative();
//    float neckSize;
//
//    for (int i = 0; i < DragonModel.VERTS_NECK; i++) {
//      float vertMulti = (i + 1) / (float)DragonModel.VERTS_NECK;
//
//      float baseRotX = MathX.cos((float) i * 0.45f + animBase) * 0.15f;
//      baseRotX *= MathX.lerp(0.2f, 1, flutter);
//      baseRotX *= MathX.lerp(1, 0.2f, sit);
//      float ofsRotX = MathX.sin(vertMulti * MathX.PI_F * 0.9f) * 0.75f;
//
//      // basic up/down movement
//      model.neck.rotateAngleX = baseRotX;
//      // reduce rotation when on ground
//      model.neck.rotateAngleX *= MathX.slerp(1, 0.5f, walk);
//      // flex neck down when hovering
//      model.neck.rotateAngleX += (1 - speed) * vertMulti;
//      // lower neck on low health
//      model.neck.rotateAngleX -= MathX.lerp(0, ofsRotX, ground * health);
//      // use looking yaw
//      model.neck.rotateAngleY = MathX.toRadians(lookYaw) * vertMulti * speed;
//
//      // update scale
//      model.neck.renderScaleX = model.neck.renderScaleY = MathX.lerp(1.6f, 1, vertMulti);
//      model.neck.renderScaleZ = 0.6f;
//
//      // hide the first and every second scale
//      model.neckScale.isHidden = i % 2 != 0 || i == 0;
//
//      // update proxy
//      model.neckProxy[i].update();
//
//      // move next proxy behind the current one
//      neckSize = DragonModel.NECK_SIZE * model.neck.renderScaleZ - 1.4f;
//      model.neck.rotationPointX -= MathX.sin(model.neck.rotateAngleY) * MathX.cos(model.neck.rotateAngleX) * neckSize;
//      model.neck.rotationPointY += MathX.sin(model.neck.rotateAngleX) * neckSize;
//      model.neck.rotationPointZ -= MathX.cos(model.neck.rotateAngleY) * MathX.cos(model.neck.rotateAngleX) * neckSize;
//    }
//
//    model.head.rotateAngleX = MathX.toRadians(lookPitch) + (1 - speed);
//    model.head.rotateAngleY = model.neck.rotateAngleY;
//    model.head.rotateAngleZ = model.neck.rotateAngleZ * 0.2f;
//
//    model.head.rotationPointX = model.neck.rotationPointX;
//    model.head.rotationPointY = model.neck.rotationPointY;
//    model.head.rotationPointZ = model.neck.rotationPointZ;
//
//    model.jaw.rotateAngleX = jaw * 0.75f;
//    model.jaw.rotateAngleX += (1 - MathX.sin(animBase)) * 0.1f * flutter;
//  }

  private EntityTameableDragon dragon;
  private final int NUMBER_OF_NECK_SEGMENTS;

}
