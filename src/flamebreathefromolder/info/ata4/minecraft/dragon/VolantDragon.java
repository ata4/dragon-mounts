/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon;

import info.ata4.minecraft.MathF;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.*;

/**
 * Base class for flying dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class VolantDragon extends Dragon {
    
    private static final Logger L = mod_DragonMounts.getLogger();
    
    public static final double TARGET_DIST_MIN = 100;
    public static final double TARGET_DIST_MAX = 32768;
    
    protected double targetX = 0;
    protected double targetY = 100;
    protected double targetZ = 0;
    protected Entity targetEntity;
    
    protected boolean randomTarget = true;
    protected boolean derangeTarget = true;
    protected boolean forceNewTarget = true;
    
    // debug options
    boolean renderTarget = false;
    
    public VolantDragon(World world) {
        super(world);
        
        noClip = true;
    }

    @Override
    public void updateAirActionState() {
        super.updateAirActionState();
        
        // initial target
        if (targetX == 0 && targetY == 100 && targetZ == 0) {
            targetX = posX;
            targetY = posY;
            targetZ = posZ;
            setRandomTarget();
        }
        
        double deltaX = targetX - posX;
        double deltaY = targetY - posY;
        double deltaZ = targetZ - posZ;
        double targetDist = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        
        // set target entity position as target, if set
        if (targetEntity != null) {
            targetX = targetEntity.posX;
            targetZ = targetEntity.posZ;
            
            double deltaXEnt = targetX - posX;
            double deltaZEnt = targetZ - posZ;
            double targetDistEnt = deltaXEnt * deltaXEnt + deltaZEnt * deltaZEnt;
            double targetDistEntSqrt = Math.sqrt(targetDistEnt);
            double targetYOfs = (0.4 + targetDistEntSqrt / 80D) - 1;
            
            if (targetYOfs > 10) {
                targetYOfs = 10;
            }
            
            targetY = targetEntity.boundingBox.minY + targetYOfs;
        } else if (derangeTarget) {
            targetX += rand.nextGaussian() * 2;
            targetZ += rand.nextGaussian() * 2;
        }
        
        // set a new target if forced to, if collided with the environment or if
        // the distance is too low or high.
        if (randomTarget) {
            if (forceNewTarget || targetDist < TARGET_DIST_MIN || targetDist > TARGET_DIST_MAX) {
                setRandomTarget();
            } else if (isCollided) {
                setRandomTargetBehind();
            }
        }

        
        deltaY /= MathF.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        double yLimit = 0.6;
        if (deltaY < -yLimit) {
            deltaY = -yLimit;
        }
        if (deltaY > yLimit) {
            deltaY = yLimit;
        }
        
        motionY += deltaY * 0.1;
        
        rotationYaw = MathF.normAngles(rotationYaw);
        
        double newRotYaw = 180 - (Math.atan2(deltaX, deltaZ) * 180) / Math.PI;
        double rotYawDelta = MathF.normAngles(newRotYaw - rotationYaw);
        
        double yawLimit = 50;
        if (rotYawDelta > yawLimit) {
            rotYawDelta = yawLimit;
        }
        if (rotYawDelta < -yawLimit) {
            rotYawDelta = -yawLimit;
        }
        
        Vec3D posDeltaVec = Vec3D.createVector(targetX - posX, targetY - posY, targetZ - posZ).normalize();
        Vec3D rotVec = Vec3D.createVector(
                MathF.sinL(MathF.toRadians(rotationYaw)),
                motionY,
                -MathF.cosL(MathF.toRadians(rotationYaw))).normalize();
        
        float rotVecDot = (float) (rotVec.dotProduct(posDeltaVec) + 0.5) / 1.5F;
        
        if (rotVecDot < 0) {
            rotVecDot = 0;
        }
        
        double motionDist = Math.sqrt(motionX * motionX + motionZ * motionZ) + 1;
        double motionDistLimit = motionDist;
        
        if (motionDistLimit > 40) {
            motionDistLimit = 40;
        }
        
        randomYawVelocity *= 0.8;
        randomYawVelocity += rotYawDelta * (0.7 / motionDistLimit / (double) motionDist);
        rotationYaw += randomYawVelocity * 0.1;
        
        float dist = (float) (2 / (motionDistLimit + 1));
        float speed = moveSpeed * 0.08f;
        
        moveFlying(0, -1, speed * (rotVecDot * dist + (1 - dist)));
        moveEntity(motionX, motionY, motionZ);
        
        Vec3D motionVec = Vec3D.createVector(motionX, motionY, motionZ).normalize();
        double mdamp = (motionVec.dotProduct(rotVec) + 1) / 2D;
        mdamp = 0.8 + 0.15 * mdamp;
        
        motionX *= mdamp;
        motionZ *= mdamp;
        motionY *= 0.91;
    }

    public void setRandomTarget() {
        forceNewTarget = false;
        
        if (rand.nextInt(2) == 0 && !worldObj.playerEntities.isEmpty()) {
            targetEntity = (Entity) worldObj.playerEntities.get(rand.nextInt(worldObj.playerEntities.size()));
            
            L.log(Level.FINER, "Dragon {0} entity flight target automatically set to {1}", new Object[]{entityId, targetEntity});
        } else {
            targetX += (0.5 - rand.nextDouble()) * 64;
            targetY = 70 + rand.nextDouble() * 50;
            targetZ += (0.5 - rand.nextDouble()) * 64;
            
            targetEntity = null;
            
            L.log(Level.FINER, "Dragon {0} flight target automatically set to [{1} {2} {3}]", new Object[]{entityId, targetX, targetY, targetZ});
        }
    }
    
    public void setRandomTargetBehind() {
        float dist = 8;
        float angles = rotationYaw * MathF.PI;
        float ofs =  (rand.nextFloat() - 0.5f) * 2;
        float xOfs = MathF.sinL(angles) * dist + ofs;
        float yOfs = (rand.nextFloat() * 3) + 1;
        float zOfs = MathF.cosL(angles) * dist + ofs;
        
        targetX = posX + xOfs;
        targetY = posY + yOfs;
        targetZ = posZ + zOfs;

        targetEntity = null;
        
        L.log(Level.FINER, "Dragon {0} flight target automatically set somewhere behind to [{1} {2} {3}]", new Object[]{entityId, targetX, targetY, targetZ});
    }
    
    public void setTarget(double x, double y, double z) {
        targetX = x;
        targetY = y;
        targetZ = z;
        
        L.log(Level.FINER, "Dragon {0} flight target manually set to [{1} {2} {3}]", new Object[]{entityId, targetX, targetY, targetZ});
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
        
        L.log(Level.FINER, "Dragon {0} entity flight target manually set to {1}", new Object[]{entityId, targetEntity});
    }
    
    @Override
    public boolean isFluttering() {
        // always flutter when moving slowly
        if (!onGround && moveSpeed < 0.2) {
            return true;
        }
        
        return super.isFluttering();
    }
    
    @Override
    public boolean attackPartFrom(DragonPart part, DamageSource src, int amount) {
        boolean attacked = super.attackPartFrom(part, src, amount);
        
        // turn around by selecting a new target behind
        if (attacked) {
            setRandomTargetBehind();
        }
        
        return attacked;
    }
}
