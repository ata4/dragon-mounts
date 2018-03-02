/*
 ** 2013 July 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.ground;

import com.google.common.base.Predicate;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.ai.EntityAITargetNonTamed;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonHunt extends EntityAITargetNonTamed {

	private final EntityTameableDragon dragon;

	public EntityAIDragonHunt(EntityTameableDragon dragon, Class classTarget,
	                          boolean checkSight, Predicate targetSelector) {
		super(dragon, classTarget, checkSight, targetSelector);
		this.dragon = dragon;
	}

	@Override
	public boolean shouldExecute() {
		return dragon.isAdult() && super.shouldExecute();
	}
}
