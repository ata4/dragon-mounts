/*
 ** 2012 Januar 1
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.minecraft.flame;

import info.ata4.minecraft.GameUtils;
import info.ata4.minecraft.MathF;
import net.minecraft.src.*;

/**
 * Flame particle emitter.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FlameEmitter extends Entity {
    
    private final EffectRenderer effectRender;
    private EntityLiving owner;
    
    private double dx;
    private double dy;
    private double dz;
    
    // emitter parameters
    public boolean enabled = true;
    public boolean moveWithOwner = true;
    public float spread = 0.6f;
    public int density = 16;
    public int exhaustSpeed = 12;
    public int flameSize = 4;
    public int flameLifetime = 40;
    
    // particle parameters
    public float smokeChance = 0.1f;
    public float largeSmokeChance = 0.3f;
    public boolean igniteBlocks = true;
    public boolean igniteEntities = true;
    public int igniteDamage = 2;
    public int igniteDuration = 5;
    public float igniteChance = 0.12f;
    
    public FlameEmitter(World world) {
        this(world, null);
        
    }

    public FlameEmitter(World world, EntityLiving owner) {
        super(world);
        
        this.owner = owner;
        
        // hack as a circumvention for the missing particle API
        effectRender = GameUtils.getMinecraft().effectRenderer;
        
        noClip = true;
    }
    
    public Entity getOwner() {
        return owner;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
    }
    
    @Override
    public Vec3D getLookVec() {
        return getLookVec(1);
    }

    public Vec3D getLookVec(float partialTicks) {
        if (partialTicks == 1) {
            float f1 = MathF.cosL(MathF.toRadians(-rotationYaw) - MathF.PI);
            float f3 = MathF.sinL(MathF.toRadians(-rotationYaw) - MathF.PI);
            float f5 = -MathF.cosL(MathF.toRadians(-rotationPitch));
            float f7 = MathF.sinL(MathF.toRadians(-rotationPitch));
            return Vec3D.createVector(f3 * f5, f7, f1 * f5);
        } else {
            float f2 = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
            float f4 = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks;
            float f6 = MathF.cosL(MathF.toRadians(-f4) - MathF.PI);
            float f8 = MathF.sinL(MathF.toRadians(-f4) - MathF.PI);
            float f9 = -MathF.cosL(MathF.toRadians(-f2));
            float f10 = MathF.sinL(MathF.toRadians(-f2));
            return Vec3D.createVector(f8 * f9, f10, f6 * f9);
        }
    }

    @Override
    public void onUpdate() {
        Vec3D look = owner.getLookVec();
        
        if (owner != null && moveWithOwner) {
            setPosition(owner.posX, owner.posY, owner.posZ);
            look = owner.getLookVec();
        } else {
            look = getLookVec();
        }
        
        dx = look.xCoord;
        dy = look.yCoord;
        dz = look.zCoord;
        
        if (!enabled) {
            return;
        }
        
        for (int i = 0; i < density; i++) {
            double dpx = dx * exhaustSpeed + rand.nextGaussian() * spread;
            double dpy = dy * exhaustSpeed + rand.nextGaussian() * spread;
            double dpz = dz * exhaustSpeed + rand.nextGaussian() * spread;
            
            // compensate owner's movement
            if (owner != null && moveWithOwner) {
                dpx += owner.motionX * exhaustSpeed;
                dpy += owner.motionY * exhaustSpeed;
                dpz += owner.motionZ * exhaustSpeed;
            } else {
                dpx += motionX * exhaustSpeed;
                dpy += motionY * exhaustSpeed;
                dpz += motionZ * exhaustSpeed;
            }
            
            FlameFX fx = new FlameFX(worldObj, posX, posY, posZ, dpx, dpy, dpz, this);
            effectRender.addEffect(fx);
        }
    }
}
