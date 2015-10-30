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

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
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
    private float lastRotationYawHead;

    public DragonBodyHelper(EntityTameableDragon dragon) {
        super(dragon);
        this.dragon = dragon;
    }

    /**
     * The body helper is used to rotate the Dragon based on whether it is
     * moving or not. 1) If moving - rotate the body and the head to the
     * movement direction 2) If not moving - rotate the body to face where the
     * head is looking
     */
    @Override
    public void updateRenderAngles() {
        double deltaX = dragon.posX - dragon.prevPosX;
        double deltaZ = dragon.posZ - dragon.prevPosZ;
        double distSQ = deltaX * deltaX + deltaZ * deltaZ;

        float maximumHeadBodyAngleDifference = 90;
        final float MOVEMENT_THRESHOLD_SQ = 0.0001F;
        // if flying or moving:
        // 1) snap the body yaw (renderYawOffset) to the movement direction (rotationYaw)
        // 2) constrain the head yaw (rotationYawHead) to be within +/- 90 of the body yaw (renderYawOffset)
        if (dragon.isFlying() || distSQ > MOVEMENT_THRESHOLD_SQ) {
            dragon.renderYawOffset = dragon.rotationYaw;
            float newRotationYawHead = MathX.constrainAngle(dragon.getRotationYawHead(), dragon.renderYawOffset,
                    maximumHeadBodyAngleDifference);
            dragon.rotationYawHead = newRotationYawHead;
            lastRotationYawHead = dragon.getRotationYawHead();
            turnTicks = 0;
            return;
        }

        double changeInHeadYaw = Math.abs(dragon.getRotationYawHead() - lastRotationYawHead);

        if (dragon.isSitting() || changeInHeadYaw > 15) { // dragon has moved his look position
            turnTicks = 0;
            lastRotationYawHead = dragon.getRotationYawHead();
        } else {
            turnTicks++;

            // as time increases, constrain the body yaw to an increasingly tighter zone around the head yaw
            if (turnTicks > turnTicksLimit) {
                maximumHeadBodyAngleDifference = Math.max(1 - (float) (turnTicks - turnTicksLimit) / turnTicksLimit, 0) * 75;
            }
        }

        float rotationYawHead = dragon.getRotationYawHead();
        dragon.renderYawOffset = MathX.constrainAngle(dragon.renderYawOffset, rotationYawHead, maximumHeadBodyAngleDifference);
        dragon.rotationYaw = dragon.renderYawOffset;
    }
}
