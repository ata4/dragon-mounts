/*
 ** 2013 November 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.ground;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.ai.EntityAICatchOwner;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAICatchOwnerGround extends EntityAICatchOwner  {

    public EntityAICatchOwnerGround(EntityTameableDragon dragon) {
        super(dragon);
        setMutexBits(0xffffffff);
    }

    @Override
    public void updateTask() {
        dragon.liftOff();
    }
}
