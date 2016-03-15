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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import java.util.BitSet;

/**
 * Abstract "AI" for player-controlled movements.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIDragonRide extends EntityAIDragonBase {

    protected EntityPlayer rider;

    public EntityAIDragonRide(EntityTameableDragon dragon) {
        super(dragon);
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

    @Override
    public void startExecuting() {
        dragon.getNavigator().clearPathEntity();
    }
    
    @Override
    public void updateTask() {
        double x = dragon.posX;
        double y = dragon.posY;
        double z = dragon.posZ;
                
        // control direction with movement keys
        if (rider.moveStrafing != 0 || rider.moveForward != 0) {
            Vec3 wp = rider.getLookVec();
            
            if (rider.moveForward < 0) {
                wp = wp.rotateYaw(MathX.PI_F);
            } else if (rider.moveStrafing > 0) {
                wp = wp.rotateYaw(MathX.PI_F * 0.5f);
            } else if (rider.moveStrafing < 0) {
                wp = wp.rotateYaw(MathX.PI_F * -0.5f);
            }
            
            x += wp.xCoord * 10;
            y += wp.yCoord * 10;
            z += wp.zCoord * 10;
        }
        
        // control height with custom keys
        if (isFlyUp()) {
            // lift off when pressing the fly-up key
            if (!dragon.isFlying()) {
                dragon.liftOff();
            } else {
                y += 4;
            }
        } else if (isFlyDown()) {
            y -= 4;
        }

        dragon.getMoveHelper().setMoveTo(x, y, z, 1);
    }
}
