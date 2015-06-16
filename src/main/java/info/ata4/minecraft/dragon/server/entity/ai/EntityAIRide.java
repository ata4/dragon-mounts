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
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.BitSet;

/**
 * Abstract "AI" for player-controlled movements.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class EntityAIRide extends EntityAIBase {

    protected final EntityTameableDragon dragon;
    protected EntityPlayer rider;

    public EntityAIRide(EntityTameableDragon dragon) {
        this.dragon = dragon;
        setMutexBits(0xffffffff);
    }
    
    protected boolean isFlyUp() {
        return getControlFlag(0);
    }
    
    protected boolean isFlyDown() {
        return getControlFlag(1);
    }
    
    private boolean getControlFlag(int index) {
        BitSet controlFlags = dragon.getControlFlags();
        return controlFlags == null ? false : controlFlags.get(index);
    }
    
    @Override
    public boolean shouldExecute() {   
        rider = dragon.getRidingPlayer();
        return rider != null;
    }
}
