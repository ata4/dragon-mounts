/*
 ** 2012 April 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.ground;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAIRide;
import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import info.ata4.minecraft.dragon.server.util.ItemUtils;
import net.minecraft.init.Items;
import net.minecraft.util.Vec3;

/**
 * AI for player-controlled ground movements.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIRideGround extends EntityAIRide {

    private static final float PLAYER_SPEED = 0.98f;
    private final double speed;

    public EntityAIRideGround(EntityTameableDragon dragon, double speed) {
        super(dragon);
        this.speed = speed;
    }

    @Override
    public void startExecuting() {
        dragon.getNavigator().clearPathEntity();
    }

    @Override
    public void updateTask() {
        super.updateTask();

        float speedX = rider.moveForward / PLAYER_SPEED;
        float speedY = rider.moveStrafing / PLAYER_SPEED;

        if (ItemUtils.hasEquipped(rider, Items.carrot_on_a_stick)) {
            speedX = 1;
        }

        float speedPlayer = Math.max(Math.abs(speedX), Math.abs(speedY));

        Vec3 look = rider.getLookVec();
        float dir = Math.min(speedX, 0) * -1;
        dir += speedY / (speedX * 2 + (speedX < 0 ? -2 : 2));
        if (dir != 0) {
            look = look.rotateYaw((float) Math.PI * dir);
        }

        if (speedPlayer > 0) {
            dragon.getMoveHelper().setMoveTo(dragon.posX + look.xCoord, dragon.posY, dragon.posZ + look.zCoord, speed * speedPlayer);
        }

        // if we're breathing at a target, look at it
        BreathWeaponTarget breathWeaponTarget = dragon.getBreathHelper().getPlayerSelectedTarget();
        if (breathWeaponTarget != null) {
            Vec3 dragonEyePos = dragon.getPositionVector().addVector(0, dragon.getEyeHeight(), 0);
            breathWeaponTarget.setEntityLook(dragon.worldObj, dragon.getLookHelper(), dragonEyePos,
                    dragon.getHeadYawSpeed(), dragon.getHeadPitchSpeed());
        }
        // lift off when pressing the fly-up key
        if (isFlyUp()) {
            dragon.liftOff();
        }
    }
}
