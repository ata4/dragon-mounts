package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.Vec3;

/**
* Created by TGG on 24/06/2015.
*/
public class DragonHeadPositionHelper
{
  public DragonHeadPositionHelper(EntityTameableDragon parent)
  {
    dragon = parent;
  }

  public Vec3 getThroatPosition()
  {
    float eyeHeight = dragon.getEyeHeight();
    Vec3 posVec = dragon.getPositionVector();
    float yaw = dragon.rotationYaw;
    float yawHead = dragon.rotationYawHead;
    float getYawHead = dragon.getRotationYawHead();
    float pitch = dragon.rotationPitch;

//    System.out.println("getThroatPosition():" + (dragon.worldObj.isRemote ? "client" : "server")
//            + " eyeHeight:" + eyeHeight + " posVec:" + posVec + " yaw:" + yaw + " yawHead:" + yawHead + " getYawHead:" + getYawHead
//           + " pitch:" + pitch);

    return new Vec3(0, 0, 0);
  }

  public void setHeadLocation(HeadLocation headLocation) {
    this.headLocation = headLocation;
  }

  public static class HeadLocation
  {
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
  }

  private HeadLocation headLocation = new HeadLocation();


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
}
