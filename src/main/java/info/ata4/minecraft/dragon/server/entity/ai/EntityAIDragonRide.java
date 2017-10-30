/*
 ** 2012 March 18
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
import info.ata4.minecraft.dragon.util.reflection.PrivateAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * Abstract "AI" for player-controlled movements.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonRide extends EntityAIDragonBase implements PrivateAccessor {

	protected EntityPlayer rider;

	public EntityAIDragonRide(EntityTameableDragon dragon) {
		super(dragon);
		setMutexBits(0xffffffff);
	}

	@Override
	public boolean shouldExecute() {
		rider = dragon.getRidingPlayer();
		return rider != null;
	}

	@Override
	public void startExecuting() {
		dragon.getNavigator().clearPath();
	}

	@Override
	public void updateTask() {
		double x = dragon.posX;
		double y = dragon.posY;
		double z = dragon.posZ;

		// control direction with movement keys
		if (rider.moveStrafing != 0 || rider.moveForward != 0) {
			Vec3d wp = rider.getLookVec();

			if (rider.moveForward < 0) {
				wp = wp.rotateYaw(MathX.PI_F);
			} else if (rider.moveStrafing > 0) {
				wp = wp.rotateYaw(MathX.PI_F * 0.5f);
			} else if (rider.moveStrafing < 0) {
				wp = wp.rotateYaw(MathX.PI_F * -0.5f);
			}

			x += wp.x * 10;
			y += wp.y * 10;
			z += wp.z * 10;
		}

		// lift off with a jump
		if (!dragon.isFlying()) {
			if (entityIsJumping(rider)) {
				dragon.liftOff();
			}
		}

		dragon.getMoveHelper().setMoveTo(x, y, z, 1);
	}
}
