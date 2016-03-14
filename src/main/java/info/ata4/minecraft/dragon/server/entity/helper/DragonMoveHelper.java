/*
** 2016 March 13
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.Vec3;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMoveHelper extends EntityMoveHelper {

    private final EntityTameableDragon dragon;
    private final float YAW_SPEED = 5;
    
    public DragonMoveHelper(EntityTameableDragon dragon) {
        super(dragon);
        this.dragon = dragon;
    }

    @Override
    public void onUpdateMoveHelper() { 
        // original movement behavior if the entity isn't flying
        if (!dragon.isFlying()) {
            super.onUpdateMoveHelper();
            return;
        }
        
        Vec3 dragonPos = dragon.getPositionVector();
        Vec3 movePos = new Vec3(posX, posY, posZ);
        
        // get direction vector by subtracting the current position from the
        // target position and normalizing the result
        Vec3 dir = movePos.subtract(dragonPos).normalize();
        
        // get euclidean distance to target
        double dist = dragonPos.distanceTo(movePos);
        
        // move towards target if it's far enough away
        if (dist > dragon.width) {
            double flySpeed = dragon.getEntityAttribute(EntityTameableDragon.MOVE_SPEED_AIR).getAttributeValue();

            // update velocity to approach target
            dragon.motionX = dir.xCoord * flySpeed;
            dragon.motionY = dir.yCoord * flySpeed;
            dragon.motionZ = dir.zCoord * flySpeed;
        } else {
            // just slow down and hover at current location
            dragon.motionX *= 0.8;
            dragon.motionY *= 0.8;
            dragon.motionZ *= 0.8;
            
            dragon.motionY += Math.sin(dragon.ticksExisted / 5) * 0.03;
        }
        
        // face entity towards target
        if (dist > 2.5E-7) {
            float newYaw = (float) Math.toDegrees(Math.PI * 2 - Math.atan2(dir.xCoord, dir.zCoord));
            dragon.rotationYaw = limitAngle(dragon.rotationYaw, newYaw, YAW_SPEED);
        }
        
        // apply movement
        dragon.moveEntity(dragon.motionX, dragon.motionY, dragon.motionZ);
    }
}
