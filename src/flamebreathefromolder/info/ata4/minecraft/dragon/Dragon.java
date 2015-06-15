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
import info.ata4.minecraft.flame.FlameEmitter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.src.*;

/**
 * Here be dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Dragon extends EntityLiving {
    
    private static final Logger L = mod_DragonMounts.getLogger();
    private static final boolean[] BLOCK_BLACKLIST = new boolean[Block.blocksList.length];
    
    static {
        BLOCK_BLACKLIST[Block.bedrock.blockID] = true;
        BLOCK_BLACKLIST[Block.dragonEgg.blockID] = true;
        BLOCK_BLACKLIST[Block.obsidian.blockID] = true;
        BLOCK_BLACKLIST[Block.whiteStone.blockID] = true;
    }

    protected DragonPart parts[];
    protected DragonPart head;
    protected DragonPart neck;
    protected DragonPart body;
    protected DragonPart tail1;
    protected DragonPart tail2;
    protected DragonPart tail3;
    protected DragonPart wingLeft;
    protected DragonPart wingRight;
    protected FlameEmitter flameBreath;
    
    protected int flameBreathTicks;
    protected int noClipTicks;
    
    protected double trail[][] = new double[64][2];
    protected int trailIndex = -1;

    protected int attackDmgTouch = 8;
    
    private float anim, prevAnim;
    private float ground, prevGround;
    private float flutter, prevFlutter;
    
    private boolean hitboxSpawned;
    private Vec3D pvec = Vec3D.createVectorHelper(0, 0, 0);
    
    // debug options
    boolean renderHitbox = false;
    boolean updateModel = false;
    boolean debugRotation = false;

    public Dragon(World world) {
        super(world);

        parts = new DragonPart[]{
            head =      new DragonPart(this, "head", 2, 2),
            neck =      new DragonPart(this, "neck", 2, 2),
            body =      new DragonPart(this, "body", 3, 3),
            tail1 =     new DragonPart(this, "tail", 2, 2),
            tail2 =     new DragonPart(this, "tail", 2, 2),
            tail3 =     new DragonPart(this, "tail", 2, 2),
            wingLeft =  new DragonPart(this, "wing", 6, 2),
            wingRight = new DragonPart(this, "wing", 6, 2)
        };
        
        flameBreath = new FlameEmitter(world, this);
        flameBreath.exhaustSpeed = 18;
        flameBreath.spread = 1;
        flameBreath.moveWithOwner = false; // we're doing this ourselves
        
        setSize(3, 3);
        
        texture = "/mob/dragon/skin_black.png";
        moveSpeed = 1;
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        stepHeight = 1;
        
        // assume that we're spawning on ground
        ground = prevGround = 1;
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        noClipTicks = nbt.getInteger("NoClipTicks");
        flameBreathTicks = nbt.getInteger("FlameBreathTicks");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("NoClipTicks", noClipTicks);
        nbt.setInteger("FlameBreathTicks", flameBreathTicks);
    }
    
    @Override
    public void onLivingUpdate() {
        if (!isMovementBlocked() && !worldObj.multiplayerWorld) {
            updateEntityActionState();
            updateTrail();
            updateHitbox();
        }
        
        updateAnimation();
    }
    
    @Override
    protected void updateEntityActionState() {
        // enable flame breath
        if (flameBreathTicks > 0) {
            flameBreathTicks--;
            flameBreath.enabled = true;
        } else {
            flameBreath.enabled = false;
        }
        
        // enable noclip
        if (noClipTicks > 0) {
            noClipTicks--;
            noClip = true;
            
            // that's actually purple dust
            for (int i = 0; i < 16; i++) {
                spawnBodyParticle("reddust");
            }
        } else {
            noClip = false;
        }
        
        if (onGround) {
            updateGroundActionState();
        } else {
            updateAirActionState();
        }
    }
    
    public boolean attackPartFrom(DragonPart part, DamageSource src, int amount) {
        if (isImmuneToDamage(src)) {
            return false;
        }
        
        // reduce damage for non-head hits
        if (part != head) {
            amount = amount / 4 + 1;
        }
        
        L.log(Level.FINE, "Dragon {0} got attacked on {1} by {2}:{3}", new Object[]{entityId, part, src.getDamageType(), amount});

        return attackEntityFrom(src, amount);
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource src, int amount) {
        if (isImmuneToDamage(src)) {
            return false;
        }
        
        return super.attackEntityFrom(src, amount);
    }
    
    public boolean isImmuneToDamage(DamageSource src) {
        Entity srcEnt = src.getEntity();
        if (srcEnt != null) {
            // ignore own damage
            if (srcEnt == this) {
                return true;
            }
            
            // ignore damage from rider
            if (srcEnt == riddenByEntity) {
                return true;
            }
        }
        
        return false;
    }
    
    protected void spawnBodyParticle(String effect) {
        double ox, oy, oz;

        if (effect.equals("explode")) {
            ox = rand.nextGaussian() * 0.5;
            oy = rand.nextGaussian() * 0.5;
            oz = rand.nextGaussian() * 0.5;
        } else if (effect.equals("cloud")) {
            ox = (rand.nextDouble() - 0.5) * 0.1;
            oy = rand.nextDouble() * 0.2;
            oz = (rand.nextDouble() - 0.5) * 0.1;
        } else if (effect.equals("reddust")) {
            ox = 0.8;
            oy = 0;
            oz = 0.8;
        } else {
            ox = 0;
            oy = 0;
            oz = 0;
        }
        
        if (hitboxSpawned) {
            // try to spawn the particle inside the AABB of the body or other parts
            for (int i = 0; i < 8; i++) {
                pvec.xCoord = posX + (rand.nextDouble() - 0.5) * width * 6;
                pvec.yCoord = posY + (rand.nextDouble() - 0.5) * height * 4;
                pvec.zCoord = posZ + (rand.nextDouble() - 0.5) * width * 6;

                if (boundingBox.isVecInside(pvec)) {
                    worldObj.spawnParticle(effect, pvec.xCoord, pvec.yCoord, pvec.zCoord, ox, oy, oz);
                    return;
                }

                for (DragonPart part : parts) {
                    if (part.boundingBox.isVecInside(pvec)) {
                        worldObj.spawnParticle(effect, pvec.xCoord, pvec.yCoord, pvec.zCoord, ox, oy, oz);
                        return;
                    }
                }
            }
        } else {
            // use generic random box spawning
            pvec.xCoord = posX + (rand.nextDouble() - 0.5) * width * 4;
            pvec.yCoord = posY + (rand.nextDouble() - 0.5) * height * 2;
            pvec.zCoord = posZ + (rand.nextDouble() - 0.5) * width * 4;
            
            worldObj.spawnParticle(effect, pvec.xCoord, pvec.yCoord, pvec.zCoord, ox, oy, oz);
        }
    }
    
    protected void updateGroundActionState() {

    }
    
    protected void updateAirActionState() {

    }

    protected void updateTrail() {
        // set initial position
        if (trailIndex < 0) {
            for (int i = 0; i < trail.length; i++) {
                trail[i][0] = rotationYaw;
                trail[i][1] = posY;
            }
        }
        
        // restart pointer at end
        if (++trailIndex == trail.length) {
            trailIndex = 0;
        }
        
        if (debugRotation) {
            rotationYaw = prevRotationYaw = (float) Math.cos(ticksExisted * Math.PI * 0.005) * 90;
            trail[trailIndex][0] = rotationYaw;
            trail[trailIndex][1] = posY + Math.sin(ticksExisted * Math.PI * 0.02) * 4;
        } else {
            trail[trailIndex][0] = rotationYaw;
            trail[trailIndex][1] = posY;
        }  
    }
    
    protected void updateHitbox() {
        if (!isEntityAlive()) {
            return;
        }
        
        if (!hitboxSpawned) {
            hitboxSpawned = true;
            
            for (DragonPart part : parts) {
                if (!worldObj.spawnEntityInWorld(part)) {
                    L.log(Level.FINE, "Dragon {0} couldn''t spawn hitbox {1}", new Object[]{entityId, part});
                    hitboxSpawned = false;
                }
            }
            
            if (!worldObj.spawnEntityInWorld(flameBreath)) {
                L.log(Level.FINE, "Dragon {0} couldn''t spawn fire breath", new Object[]{entityId});
                hitboxSpawned = false;
            }
            
            if (hitboxSpawned) {
                L.log(Level.FINE, "Dragon {0} hitbox spawned successfully", entityId);
            } else {
                L.log(Level.FINE, "Dragon {0} hitbox not spawned properly!", entityId);
            }
        }
        
        // fix hitbox offset
        Vec3D look = getLookVec();
        double bbPosX = posX - look.xCoord * 2 * getBodySize();
        double bbPosY = posY;
        double bbPosZ = posZ - look.zCoord * 2 * getBodySize();
        
        renderYawOffset = rotationYaw;
        
        body.setLocationAndAngles(bbPosX, bbPosY, bbPosZ, 0, 0);
        
        float rotAngles = MathF.toRadians(rotationYaw);
        
        float wingOfsX = MathF.cosL(rotAngles) * wingLeft.width;
        float wingOfsY = wingLeft.height;
        float wingOfsZ = MathF.sinL(rotAngles) * wingLeft.width;
        
        if (onGround) {
            wingOfsX = 0;
            wingOfsZ = 0;
        } else {
            wingOfsX *= 0.8;
            wingOfsZ *= 0.8;
        }
        
        wingLeft.setLocationAndAngles(
                bbPosX + wingOfsX,
                bbPosY + wingOfsY,
                bbPosZ + wingOfsZ,
                0, 0);
        wingRight.setLocationAndAngles(
                bbPosX - wingOfsX,
                bbPosY + wingOfsY,
                bbPosZ - wingOfsZ,
                0, 0);
        
        double trailNeck[] = getTrail(DragonModel.VERTS_TAIL);
        
        for (int i = 0; i < 2; i++) {
            DragonPart neckPart = null;

            if (i == 0) {
                neckPart = neck;
            } else if (i == 1) {
                neckPart = head;
            }

            double trailNeck2[] = getTrail(DragonModel.VERTS_NECK - i);

            float neckOfsX = MathF.sinL(rotAngles) * neck.width * (i + 2);
            float neckOfsY = (neck.height / 2f) + (float) (trailNeck2[1] - trailNeck[1]) / 2f;
            float neckOfsZ = MathF.cosL(rotAngles) * neck.width * (i + 2);
            
            if (onGround) {
                if (i == 0) {
                    neckOfsY += 0.5 * getBodySize(); 
                } else if (i == 1) {
                    neckOfsY += 1 * getBodySize();
                }
            }

            neckPart.setLocationAndAngles(
                    bbPosX + neckOfsX,
                    bbPosY + neckOfsY,
                    bbPosZ - neckOfsZ,
                    0, 0);
            
            if (i == 1) {
                flameBreath.setLocationAndAngles(
                        bbPosX + neckOfsX,
                        bbPosY + neckOfsY + (neck.height / 2.0) - 0.25,
                        bbPosZ - neckOfsZ,
                        rotationYaw + 180, rotationPitch + 15);
                flameBreath.motionX = motionX;
                flameBreath.motionY = motionY;
                flameBreath.motionZ = motionZ;
            }
        }
 
        trailNeck = getTrail(DragonModel.VERTS_NECK);
        
        for (int i = 0; i < 3; i++) {
            DragonPart tail = null;
            
            if (i == 0) {
                tail = tail1;
            } else if (i == 1) {
                tail = tail2;
            } else {
                tail = tail3;
            }
            
            double trailTail[] = getTrail(DragonModel.VERTS_TAIL + i * 2);
            
            float tailOfsX = MathF.sinL(rotAngles) * neck.width * (i + 1.5f);
            float tailOfsY = (neck.height / 2f) + (float) (trailTail[1] - trailNeck[1]) / 2f;
            float tailOfsZ = MathF.cosL(rotAngles) * neck.width * (i + 1.5f);
            
            tail.setLocationAndAngles(
                    bbPosX - tailOfsX,
                    bbPosY + tailOfsY,
                    bbPosZ + tailOfsZ,
                    0, 0);
        }
        
        // destroy blocks in noclip mode
        if (noClip) {
            boolean collide = touchWorld(boundingBox) | touchWorld(head.boundingBox) | touchWorld(body.boundingBox);
            
            if (riddenByEntity != null) {
                collide |= touchWorld(riddenByEntity.boundingBox.expand(1, 1, 1));
            }
            
            if (!isCollided) {
                isCollided = collide;
            }
        }
        
        // damage and push away entities inside the hitboxes
        if (onGround) {
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(1, 1, 1)), false);
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, head.boundingBox.expand(1, 1, 1)), false);
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, tail1.boundingBox.expand(1, 1, 1)), false);
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, tail2.boundingBox.expand(1, 1, 1)), false);
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, tail3.boundingBox.expand(1, 1, 1)), false);
        } else {
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, wingLeft.boundingBox.expand(3, 2, 3).offset(0, -2, 0)), true);
            pushEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, wingRight.boundingBox.expand(3, 2, 3).offset(0, -2, 0)), true);
            attackEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, head.boundingBox.expand(1, 1, 1)));
        }
    }
    
    protected void updateAnimation() {
        prevAnim = anim;
        
        if (health <= 0) {
            return;
        }

        // update main animation cycle
        float animAdd = 0.025f;
        
        // depend speed on movement
        if (!onGround) {
            animAdd = 0.2f / (MathF.sqrt(motionX * motionX + motionZ * motionZ) * 10 + 1);
            animAdd *= (float) Math.pow(2, motionY) * 1.5f;
        }
        
        // avoid animation playback being too quick
        animAdd = MathF.clamp(animAdd, 0, 0.08f);
        
        anim += animAdd;
        
        // update ground transition
        prevGround = ground;
        
        if (onGround && ground < 1) {
            ground *= 0.95f;
            ground += 0.08f;
        }
        if (!onGround && ground > 0) {
            ground -= 0.2f;
        }
        ground = MathF.clamp(ground, 0, 1);
        
        // update flutter transition
        prevFlutter = flutter;
        
        boolean fluttering = isFluttering();
        
        if (fluttering && flutter < 1) {
            flutter += 0.1f;
        }
        if (!fluttering && flutter > 0) {
            flutter -= 0.1f;
        }
        flutter = MathF.clamp(flutter, 0, 1);
    }
    
    protected void pushEntities(List list, boolean extraPush) {
        double pushX = (boundingBox.minX + boundingBox.maxX) / 2D;
        double pushZ = (boundingBox.minZ + boundingBox.maxZ) / 2D;
        
        for (Object obj : list) {
            Entity entity = (Entity) obj;
            
            if (entity.canBePushed() && entity instanceof EntityLiving) {
                entity.applyEntityCollision(this);
                
                if (extraPush) {
                    double velX = entity.posX - pushX;
                    double velY = entity.posZ - pushZ;
                    double velDist = velX * velX + velY * velY;

                    entity.addVelocity((velX / velDist) * 4, 0.2, (velY / velDist) * 4);
                }
            }
        }
    }

    protected void attackEntities(List list) {
        for (Object obj : list) {
            Entity entity = (Entity) obj;
            
            // ignore our body parts
            if (entity instanceof DragonPart && ((DragonPart) entity).base == this) {
                continue;
            }
            
            // ignore fire emitter
            if (entity == flameBreath) {
                continue;
            }
            
            // ignore rider
            if (riddenByEntity != null && obj == riddenByEntity) {
                continue;
            }
            
            L.log(Level.FINER, "Dragon {0} attacked entity {1} by touching", new Object[]{entityId, entity});
            
            if (entity instanceof EntityLiving && entity != riddenByEntity) {
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), attackDmgTouch);
            }
        }
    }

    @Override
    public Vec3D getLook(float f) {
        // deal with the fact that this entity is actually moving backwards...
        Vec3D v = super.getLook(f);
        v.xCoord *= -1;
        v.yCoord *= -1;
        v.zCoord *= -1;
        return v;
    }
    
    protected boolean touchWorld(AxisAlignedBB bb) {
        int minX = (int) Math.floor(bb.minX);
        int minY = (int) Math.floor(bb.minY);
        int minZ = (int) Math.floor(bb.minZ);
        int maxX = (int) Math.floor(bb.maxX);
        int maxY = (int) Math.floor(bb.maxY);
        int maxZ = (int) Math.floor(bb.maxZ);
        
        boolean collided = false;
        boolean destroyed = false;
        
        for (int k1 = minX; k1 <= maxX; k1++) {
            for (int l1 = minY; l1 <= maxY; l1++) {
                for (int i2 = minZ; i2 <= maxZ; i2++) {
                    int j2 = worldObj.getBlockId(k1, l1, i2);
                    if (j2 == 0) {
                        continue;
                    }
                    if (BLOCK_BLACKLIST[j2]) {
                        collided = true;
                    } else {
                        destroyed = true;
                        worldObj.setBlockWithNotify(k1, l1, i2, 0);
                    }
                }
            }
        }

        if (destroyed) {
            double px = bb.minX + (bb.maxX - bb.minX) * rand.nextDouble();
            double py = bb.minY + (bb.maxY - bb.minY) * rand.nextDouble();
            double pz = bb.minZ + (bb.maxZ - bb.minZ) * rand.nextDouble();
            
            worldObj.spawnParticle("largeexplode", px, py, pz, 0, 0, 0);
            
            if (rand.nextFloat() > 0.3f) {
                worldObj.playSoundEffect(px, py, pz, "random.fizz", 0.3f, 1F + (rand.nextFloat() - rand.nextFloat()) * 0.5F);
            }
        }
        
        return collided;
    }
    
    @Override
    public void spawnExplosionParticle() {
        for (int i = 0; i < 512; i++) {
            spawnBodyParticle("explode");
        }
    }
    
    @Override
    public DragonPart[] getParts() {
        return parts;
    }
    
    public double[] getTrail(int index, float partialTicks) {
        if (health <= 0) {
            partialTicks = 0;
        }
        
        partialTicks = 1 - partialTicks;
        int i1 = trailIndex - index * 1 & (trail.length - 1);
        int i2 = trailIndex - index * 1 - 1 & (trail.length - 1);
        
        double tr[] = new double[2];
        double v1 = trail[i1][0];
        double v2 = MathF.normAngles(trail[i2][0] - v1);
        
        tr[0] = v1 + v2 * (double) partialTicks;
        
        v1 = trail[i1][1];
        v2 = trail[i2][1] - v1;
        
        tr[1] = v1 + v2 * (double) partialTicks;
        
        return tr;
    }
    
    public double[] getTrail(int index) {
        return getTrail(index, 1);
    }
    
    public float getBodySize() {
        return 1;
    }
    
    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected void setSize(float width, float height) {
        super.setSize(width * getBodySize(), height * getBodySize());
    }

    @Override
    public int getMaxHealth() {
        return 200;
    }
    
    public float getAnimTime(float partialTicks) {
        return MathF.interpLin(prevAnim, anim, partialTicks);
    }
    
    public float getGroundTime(float partialTicks) {
        return MathF.interpLin(prevGround, ground, partialTicks);
    }
    
    public float getFlutterTime(float partialTicks) {
        return MathF.interpLin(prevFlutter, flutter, partialTicks);
    }
    
    public boolean isSaddled() {
        return false;
    }
    
    public boolean isFluttering() {
        return !onGround && (isCollided || posY - lastTickPosY > -0.1);
    }
    
    public boolean isOnGround() {
        return onGround;
    }
    
   public boolean isFireBreathing() {
        return flameBreath.enabled;
    }
    
    public int getDeathTime() {
        return deathTime;
    }
    
    public int getMaxDeathTime() {
        return 140;
    }
    
    public boolean showOverlay() {
        return false;
    }
    
    @Override
    public void setEntityDead() {
        super.setEntityDead();
        
        for (DragonPart part : parts) {
            part.setEntityDead();
        }
        
        flameBreath.setEntityDead();
    }

    @Override
    protected void onDeathUpdate() {
        if (riddenByEntity != null) {
            riddenByEntity.mountEntity(null);
        }
        
        flameBreathTicks = 0;
        
        deathTime++;

        if (deathTime < getMaxDeathTime() - 20) {
            for (int i = 0; i < 16; i++) {
                spawnBodyParticle("cloud");
            }
        }
        
        if (deathTime == getMaxDeathTime()) {
            onEntityDeath();
            setEntityDead();
        }
    }
}
