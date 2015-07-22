/*
 ** 2012 March 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai;

import info.ata4.minecraft.dragon.client.render.FlameBreathFX;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.util.DataLogger;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.entity.EntityBodyHelper;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonBodyHelper extends EntityBodyHelper {

    private EntityTameableDragon dragon;
    private int turnTicks;
    private int turnTicksLimit = 20;
    private float lastStableRotationYawHead;

    public DragonBodyHelper(EntityTameableDragon dragon) {
        super(dragon);
        this.dragon = dragon;
    }

    @Override
    public void updateRenderAngles() {
        double deltaX = dragon.posX - dragon.prevPosX;
        double deltaY = dragon.posZ - dragon.prevPosZ;
        double distSQ = deltaX * deltaX + deltaY * deltaY;
        
        float yawSpeed = 90;
        final float MOVEMENT_THRESHOLD_SQ = 0.0001F;
        // if flying or moving:
        // 1) snap the body yaw (renderYawOffset) to the movement direction (rotationYaw)
        // 2) constrain the head yaw (rotationYawHead) to be within +/- 90 of the body yaw (renderYawOffset)
        if (dragon.isFlying() || distSQ > MOVEMENT_THRESHOLD_SQ) {
          System.out.format(
                  "%s- moving0  rotationYawHead:%4.0f(%4.0f), renderYawOffset:%4.0f(%4.0f), rotationYaw:%4.0f(%4.0f)\n",
                  dragon.isClient() ? "C" : "S",
                  dragon.getRotationYawHead(), MathX.normDeg(dragon.getRotationYawHead()),
                  dragon.renderYawOffset, MathX.normDeg(dragon.renderYawOffset),
                  dragon.rotationYaw, MathX.normDeg(dragon.rotationYaw));

//          System.out.println("DBH:headFromBody old:" + dragon.renderYawOffset + ", " + dragon.getRotationYawHead());
            dragon.renderYawOffset = dragon.rotationYaw;
            float newRotationYawHead = MathX.updateRotation(dragon.renderYawOffset, dragon.getRotationYawHead(),
                                                            yawSpeed);
//            dragon.setRotationYawHead(newRotationYawHead);
            dragon.rotationYawHead = newRotationYawHead;
            lastStableRotationYawHead = dragon.getRotationYawHead();
          System.out.format(
                  "%s- moving1  rotationYawHead:%4.0f(%4.0f), renderYawOffset:%4.0f(%4.0f), rotationYaw:%4.0f(%4.0f)\n",
                  dragon.isClient() ? "C" : "S",
                  dragon.getRotationYawHead(), MathX.normDeg(dragon.getRotationYawHead()),
                  dragon.renderYawOffset, MathX.normDeg(dragon.renderYawOffset),
                  dragon.rotationYaw, MathX.normDeg(dragon.rotationYaw));
            turnTicks = 0;
            return;
        }
        
        double yawDiff = Math.abs(dragon.getRotationYawHead() - lastStableRotationYawHead);

        if (dragon.isSitting() || yawDiff > 15) {
            turnTicks = 0;
            lastStableRotationYawHead = dragon.getRotationYawHead();
        } else {
            turnTicks++;

            if (turnTicks > turnTicksLimit) {
                yawSpeed = Math.max(1 - (float) (turnTicks - turnTicksLimit) / turnTicksLimit, 0) * 75;
            }
        }

      // when the dragon stops moving, turn the body and the head back to the last server head position.  The
      //   vanilla rotationYawHead gets out of step between client and server.
      float trueYawHead = dragon.getRotationYawHead();
//      if (dragon.isClient()) {
//        trueYawHead = dragon.getLastRotationYawHeadFromServer();
//        final float HEAD_RESTORE_SPEED = 10;
//        dragon.rotationYawHead = MathX.updateRotation(dragon.getRotationYawHead(), trueYawHead, HEAD_RESTORE_SPEED);
//        System.out.format("C- restore towards %4.0f(%4.0f)\n", trueYawHead, MathX.normDeg(trueYawHead));
//      }
      dragon.renderYawOffset = MathX.updateRotation(trueYawHead, dragon.renderYawOffset, yawSpeed);

      String sidePreText = dragon.isClient() ? "Client" : "Server";
      String output = Float.toString(dragon.getRotationYawHead()) + "," +
              Float.toString(dragon.renderYawOffset) + "," +
              Float.toString(dragon.rotationYaw);
      DataLogger.logData(sidePreText + "-updateRenderAngles", output);


      System.out.format(
              "%s- notmove  rotationYawHead:%4.0f(%4.0f), renderYawOffset:%4.0f(%4.0f), rotationYaw:%4.0f(%4.0f)\n",
              dragon.isClient() ? "C" : "S",
              dragon.getRotationYawHead(), MathX.normDeg(dragon.getRotationYawHead()),
              dragon.renderYawOffset, MathX.normDeg(dragon.renderYawOffset),
              dragon.rotationYaw, MathX.normDeg(dragon.rotationYaw));
    }
}
