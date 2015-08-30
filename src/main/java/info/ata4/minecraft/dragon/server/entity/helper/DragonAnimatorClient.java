package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.client.model.DragonModel;
import info.ata4.minecraft.dragon.client.model.ModelPart;
import info.ata4.minecraft.dragon.client.model.anim.DragonAnimatorCommon;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.util.math.MathX;

/**
 * Created by TGG on 30/08/2015.
 */
public class DragonAnimatorClient {

  public DragonAnimatorClient(EntityTameableDragon i_dragon, DragonAnimatorCommon i_dragonAnimatorCommon) {
    dragon = i_dragon;
    dragonAnimatorCommon = i_dragonAnimatorCommon;
  }

  private DragonAnimatorCommon dragonAnimatorCommon;
  private EntityTameableDragon dragon;

//  /**
//   * Applies the animations on the model. Called every frame before the model
//   * is rendered.
//   */
//  public void updateFromAnimator(DragonModel model) {
//    dragonAnimatorCommon.updateFromAnimator();
//
//    // update flags
//    model.back.isHidden = dragon.isSaddled();
//
//    // update offsets
//    model.offsetX = dragonAnimatorCommon.getModelOffsetX();
//    model.offsetY = dragonAnimatorCommon.getModelOffsetY();
//    model.offsetZ = dragonAnimatorCommon.getModelOffsetZ();
//
//    // update pitch
//    model.pitch = dragonAnimatorCommon.getBodyPitch();
//
//    // updateFromAnimator body parts
//    animHeadAndNeck(model);
//    animTail(model);
//    animWings(model);
//    animLegs(model);
//
//  }
//
//  protected void animHeadAndNeck(DragonModel model)
//  {
//    DragonHeadPositionHelper headPositionHelper = dragon.getDragonHeadPositionHelper();
//
//    DragonHeadPositionHelper.SegmentSizePositionRotation[] segmentData =
//            headPositionHelper.getNeckSegmentPositionSizeLocations();
//
//    if (model.neckProxy.length != segmentData.length) {
//      throw new IllegalArgumentException("DragonModel.VERTS_NECK and " +
//                                                 "EntityTameableDragon.NUMBER_OF_NECK_SEGMENTS must match.");
//      // I couldn't think of a way to synch these without breaking a pile of stuff.
//    }
//    for (int i = 0; i < model.neckProxy.length; i++) {
//      copyPositionRotationLocation(model.neck, segmentData[i]);
//      // hide the first and every second scale
//      model.neckScale.isHidden = i % 2 != 0 || i == 0;
//
//      // update proxy
//      model.neckProxy[i].update();
//    }
//
//    copyPositionRotationLocation(model.neck, headPositionHelper.getNeckPositionSizeLocation());
//    copyPositionRotationLocation(model.head, headPositionHelper.getHeadPositionSizeLocation());
//
//    model.jaw.rotateAngleX = dragonAnimatorCommon.getJawRotateAngleX();
//}
//    protected void animWings(DragonModel model) {
//      // apply angles
//      model.wingArm.rotateAngleX = dragonAnimatorCommon.getWingArmRotateAngleX();
//      model.wingArm.rotateAngleY = dragonAnimatorCommon.getWingArmRotateAngleY();
//      model.wingArm.rotateAngleZ = dragonAnimatorCommon.getWingArmRotateAngleZ();
//      model.wingArm.preRotateAngleX = dragonAnimatorCommon.getWingArmPreRotateAngleX();
//      model.wingForearm.rotateAngleX = dragonAnimatorCommon.getWingForearmRotateAngleX();
//      model.wingForearm.rotateAngleY = dragonAnimatorCommon.getWingForearmRotateAngleY();
//      model.wingForearm.rotateAngleZ = dragonAnimatorCommon.getWingForearmRotateAngleZ();
//      // interpolate between folded and unfolded wing angles
//      float[] yFold = new float[] {2.7f, 2.8f, 2.9f, 3.0f};
//      float[] yUnfold = new float[] {0.1f, 0.9f, 1.7f, 2.5f};
//
//      // set wing finger angles
//      for (int i = 0; i < model.wingFinger.length; i++) {
//        model.wingFinger[i].rotateAngleX = dragonAnimatorCommon.getWingFingerRotateX(i);
//        model.wingFinger[i].rotateAngleY = dragonAnimatorCommon.getWingFingerRotateY(i);
//      }
//
//    }
//      protected void animTail(DragonModel model) {
//      }
//        protected void animLegs(DragonModel model) {
//        }
//
//          protected void copyPositionRotationLocation(ModelPart modelPart,
//            DragonHeadPositionHelper.SegmentSizePositionRotation segmentData)
//    {
//      modelPart.rotateAngleX = copyIfValid(segmentData.rotateAngleX, modelPart.rotateAngleX);
//      modelPart.rotateAngleY = copyIfValid(segmentData.rotateAngleY, modelPart.rotateAngleY);
//      modelPart.rotateAngleZ = copyIfValid(segmentData.rotateAngleZ, modelPart.rotateAngleZ);
//      modelPart.renderScaleX = copyIfValid(segmentData.scaleX, modelPart.renderScaleX);
//      modelPart.renderScaleY = copyIfValid(segmentData.scaleY, modelPart.renderScaleY);
//      modelPart.renderScaleZ = copyIfValid(segmentData.scaleZ, modelPart.renderScaleZ);
//      modelPart.rotationPointX = copyIfValid(segmentData.rotationPointX, modelPart.rotationPointX);
//      modelPart.rotationPointY = copyIfValid(segmentData.rotationPointY, modelPart.rotationPointY);
//      modelPart.rotationPointZ = copyIfValid(segmentData.rotationPointZ, modelPart.rotationPointZ);
//    }

  }
