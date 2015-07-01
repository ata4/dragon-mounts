package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.util.math.MathX;
import info.ata4.minecraft.dragon.util.math.RotatingQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by EveryoneElse on 21/06/2015.
 */
public class FlameBreathFX extends EntityFX {
  private final ResourceLocation fireballRL = new ResourceLocation("dragonmounts:entities/breath_fire");
  private Entity owner;

  public enum Power {SMALL, MEDIUM, LARGE};

  protected float currentParticleSize;
  protected float particleMaxSize;

  public float smokeChance = 0.1f;
  public float largeSmokeChance = 0.3f;
  public boolean igniteBlocks = true;
  public boolean igniteEntities = true;
  public int igniteDamage = 2;
  public int igniteDuration = 5;
  public float igniteChance = 0.12f;

  private static final float MAX_ALPHA = 0.99F;
  private static final double INITIAL_SPEED = 0.6; // blocks per tick
  private static final float AABB_RELATIVE_TO_SIZE = 0.5F;  // how big is the AABB relative to the fireball size.

  private static final double SPEED_VARIATION_ABS = 0.1;  // plus or minus this amount (3 std deviations)
  private static final double AGE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)
  private static final double SIZE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)

  public static FlameBreathFX createFlameBreathFX(World world, double x, double y, double z,
                                                  double directionX, double directionY, double directionZ,
                                                  Power power,
                                                  float partialTicksHeadStart)
  {
    Vec3 direction = new Vec3(directionX, directionY, directionZ).normalize();

    float speedFactor = 1.0F;
    float ageFactor = 1.0F;
    float sizeFactor = 1.0F;

    switch (power) {
      case SMALL: {
        speedFactor = 0.25F;
        ageFactor = 0.25F;
        sizeFactor = 0.25F;
        break;
      }
      case MEDIUM: {
        speedFactor = 0.5F;
        ageFactor = 0.5F;
        sizeFactor = 0.5F;
        break;
      }
      case LARGE: {
        speedFactor = 1.0F;
        ageFactor = 1.0F;
        sizeFactor = 1.0F;
        break;
      }

      default: {
        System.err.println("Invalid power in createFlameBreathFX:" + power);
      }
    }
todo get the breath speed right
          also the breath start position for head isnt right for hatchling
    Random rand = new Random();
    double actualMotionX = direction.xCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    double actualMotionY = direction.yCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    double actualMotionZ = direction.zCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    actualMotionX *= speedFactor * INITIAL_SPEED;
    actualMotionY *= speedFactor * INITIAL_SPEED;
    actualMotionZ *= speedFactor * INITIAL_SPEED;

    x += actualMotionX * partialTicksHeadStart;
    y += actualMotionY * partialTicksHeadStart;
    z += actualMotionZ * partialTicksHeadStart;
    return new FlameBreathFX(world, x, y, z, actualMotionX, actualMotionY, actualMotionZ,
                             DEFAULT_BALL_SIZE * sizeFactor, (int)(DEFAULT_AGE_IN_TICKS * ageFactor));
  }

  private static final float DEFAULT_BALL_SIZE = 1.0F;
  private static final int DEFAULT_AGE_IN_TICKS = 40;
  private FlameBreathFX(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    this(world, x, y, z, velocityX, velocityY, velocityZ, DEFAULT_BALL_SIZE, DEFAULT_AGE_IN_TICKS);
  }

  private FlameBreathFX(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                 float size, int age) {
    super(world, x, y, z, velocityX, velocityY, velocityZ);

    particleGravity = Blocks.fire.blockParticleGravity;  /// arbitrary block!  maybe not even required.
    particleMaxSize = size * (float)MathX.getTruncatedGaussian(rand, 1, SIZE_VARIATION_FACTOR);
    particleMaxAge = (int)(age * MathX.getTruncatedGaussian(rand, 1, AGE_VARIATION_FACTOR));
    this.particleAlpha = MAX_ALPHA;  // a value less than 1 turns on alpha blending
    currentParticleSize = getParticleSize(0.0F);

//    System.out.format("Constructor pos[x,y,z]= %.3f, %.3f, %.3f prevPos[x,y,z]= %.3f, %.3f, %.3f\n",
//            posX, posY, posZ, prevPosX, prevPosY, prevPosZ);

    float initialCollisionSize = AABB_RELATIVE_TO_SIZE * currentParticleSize;
    changeSize(initialCollisionSize, initialCollisionSize);  // using setSize causes trouble.

//    System.out.format("Constructor2 pos[x,y,z]= %.3f, %.3f, %.3f prevPos[x,y,z]= %.3f, %.3f, %.3f\n",
//            posX, posY, posZ, prevPosX, prevPosY, prevPosZ);

    //undo random velocity variation of vanilla constructor
    motionX = velocityX;
    motionY = velocityY;
    motionZ = velocityZ;

//    setPosition(x + motionX * partialTicksElapsed, y + motionY * partialTicksElapsed, z + motionZ * partialTicksElapsed);
//    prevPosX = posX - motionX;
//    prevPosY = posY - motionY;
//    prevPosZ = posZ - motionZ;

//    System.out.format("Constructor2 pos[x,y,z]= %.3f, %.3f, %.3f prevPos[x,y,z]= %.3f, %.3f, %.3f\n",
//            posX, posY, posZ, prevPosX, prevPosY, prevPosZ);


    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fireballRL.toString());
    func_180435_a(sprite);
  }

//    public FlameFX(World world, double x, double y, double z, double a, double b, double c, FlameEmitter ft) {
//        this(world, x, y, z, a, b, c, ft.flameSize, ft.flameLifetime);
//
//        owner = ft.getOwner();
//
//        smokeChance = ft.smokeChance;
//        largeSmokeChance = ft.largeSmokeChance;
//        igniteBlocks = ft.igniteBlocks;
//        igniteEntities = ft.igniteEntities;
//        igniteDamage = ft.igniteDamage;
//        igniteDuration = ft.igniteDuration;
//        igniteChance = ft.igniteChance;
//    }

  /**
   * Returns 1, which means "use a texture from the blocks + items texture sheet"
   * @return
   */
  @Override
  public int getFXLayer() {
    return 1;
  }

  // this function is used by EffectRenderer.addEffect() to determine whether depthmask writing should be on or not.
  // by default, vanilla turns off depthmask writing for entityFX with alphavalue less than 1.0
  // FlameBreathFX uses alphablending but we want depthmask writing on, otherwise translucent objects (such as water)
  //   render over the top of our breath.
  @Override
  public float func_174838_j()
  {
    return 1.0F;
  }

  @Override
  public int getBrightnessForRender(float partialTick)
  {
    return 0xf000f0;
  }

  private float getParticleSize(float fractionOfFullSize)
  {
    final float INITIAL_SIZE = 0.2F;
    return INITIAL_SIZE + (particleMaxSize - INITIAL_SIZE) * MathHelper.clamp_float(fractionOfFullSize, 0.0F, 1.0F);
  }

  @Override
  public void func_180434_a(WorldRenderer worldRenderer, Entity entity, float partialTick,
                            float yawX, float pitchXZ, float yawZ, float pitchYsinYaw, float pitchYcosYaw)
  {
    double minU = this.particleIcon.getMinU();
    double maxU = this.particleIcon.getMaxU();
    double minV = this.particleIcon.getMinV();
    double maxV = this.particleIcon.getMaxV();
    RotatingQuad tex = new RotatingQuad(minU, minV, maxU, maxV);
    Random random = new Random();
    if (random.nextBoolean()) {
      tex.mirrorLR();
    }
    tex.rotate90(random.nextInt(4));

    double scale = 0.1F * this.particleScale;
    double x = this.prevPosX + (this.posX - this.prevPosX) * partialTick - interpPosX;
    double y = this.prevPosY + (this.posY - this.prevPosY) * partialTick - interpPosY;
    double z = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTick - interpPosZ;

//    System.out.format("FlameBreathFX pos[x,y,z]= %.3f, %.3f, %.3f prevPos[x,y,z]= %.3f, %.3f, %.3f partialTick= %.2f\n",
//                      posX, posY, posZ, prevPosX, prevPosY, prevPosZ, partialTick);
//    System.out.format("FlameBreathFX [x,y,z]= %.3f, %.3f, %.3f\n", x, y, z);

    worldRenderer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
    worldRenderer.addVertexWithUV(x - yawX * scale - pitchYsinYaw * scale, y - pitchXZ * scale,
                                  z - yawZ * scale - pitchYcosYaw * scale,  tex.getU(0),  tex.getV(0));
    worldRenderer.addVertexWithUV(x - yawX * scale + pitchYsinYaw * scale, y + pitchXZ * scale,
                                  z - yawZ * scale + pitchYcosYaw * scale,  tex.getU(1),  tex.getV(1));
    worldRenderer.addVertexWithUV(x + yawX * scale + pitchYsinYaw * scale,  y + pitchXZ * scale,
                                  z + yawZ * scale + pitchYcosYaw * scale,  tex.getU(2),  tex.getV(2));
    worldRenderer.addVertexWithUV(x + yawX * scale - pitchYsinYaw * scale,  y - pitchXZ * scale,
                                  z + yawZ * scale - pitchYcosYaw * scale,  tex.getU(3),  tex.getV(3));
  }

  @Override
  public void onUpdate() {
    float lifetimeFraction = (float) particleAge / (float) particleMaxAge;
    lifetimeFraction = MathHelper.clamp_float(lifetimeFraction, 0.0F, 1.0F);

    final float YOUNG_AGE = 0.25F;
    final float OLD_AGE = 0.75F;

    if (lifetimeFraction < YOUNG_AGE) {
      float fractionalSize = MathHelper.sin(lifetimeFraction / YOUNG_AGE * (float) Math.PI / 2.0F);
      currentParticleSize = getParticleSize(fractionalSize);
      particleAlpha = MAX_ALPHA;
    } else if (lifetimeFraction < OLD_AGE) {
      currentParticleSize = getParticleSize(1.0F);
      particleAlpha = MAX_ALPHA;
    } else {
      currentParticleSize = getParticleSize(1.0F);
      particleAlpha = MAX_ALPHA * (1 - lifetimeFraction);
    }

    changeSize(currentParticleSize * AABB_RELATIVE_TO_SIZE, currentParticleSize * AABB_RELATIVE_TO_SIZE);  // note - will change posX, posY, posZ to keep centre constant when resizing

    final float PARTICLE_SCALE_RELATIVE_TO_SIZE = 5.0F; // factor to convert from particleSize to particleScale
    particleScale = PARTICLE_SCALE_RELATIVE_TO_SIZE * currentParticleSize;

    // spawn a smoke trail after some time
    if (smokeChance != 0 && rand.nextFloat() < lifetimeFraction && rand.nextFloat() <= smokeChance) {
      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, motionX * 0.5, motionY * 0.5, motionZ * 0.5);
    }

    if (particleAge++ >= particleMaxAge) {
      setDead();
      return;
    }

    // extinguish when hitting water
    if (handleWaterMovement()) {
      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, 0, 0, 0);
      setDead();
      return;
    }

//        motionY += 0.02;   what's this for?  why rise?

//    System.out.format("onUpdatePre pos[x,y,z]= %.3f, %.3f, %.3f prevPos[x,y,z]= %.3f, %.3f, %.3f\n",
//            posX, posY, posZ, prevPosX, prevPosY, prevPosZ);

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    moveEntity(motionX, motionY, motionZ);

//    System.out.format("onUpdatePost pos[x,y,z]= %.3f, %.3f, %.3f prevPos[x,y,z]= %.3f, %.3f, %.3f\n",
//            posX, posY, posZ, prevPosX, prevPosY, prevPosZ);


//        if (posY == prevPosY) {
//            motionX *= 1.1;
//            motionZ *= 1.1;
//        }

//    final double SPEED_LOSS_PERCENT_PER_SECOND = 20;
//    final double SPEED_MULT_PER_TICK = 1.0 - SPEED_LOSS_PERCENT_PER_SECOND / 100.0 / 20.0;
//    motionX *= SPEED_MULT_PER_TICK;
//    motionY *= SPEED_MULT_PER_TICK;
//    motionZ *= SPEED_MULT_PER_TICK;
//
//    if (onGround) {
//      motionX *= 0.7;
//      motionZ *= 0.7;
//    }

    // collision ages particles faster
    if (isCollided) {
      particleAge += 5;
    }


    // slow particles age very fast
    final double SPEED_THRESHOLD = INITIAL_SPEED * 0.25;
    if (motionX * motionX + motionY * motionY + motionZ * motionZ < SPEED_THRESHOLD * SPEED_THRESHOLD) {
      particleAge += 20;
    }

//        // ignite environment
//        if ((igniteEntities || igniteBlocks) && rand.nextFloat() <= igniteChance) {
//            igniteEnvironment();
//        }
  }

//    @Override
//    public boolean handleWaterMovement() {
//        return worldObj.handleMaterialAcceleration(boundingBox, Material.water, this);
//    }
//
//    @Override
//    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5) {
//        tessellator.setBrightness(240);
//        super.renderParticle(tessellator, f, f1, f2, f3, f4, f5);
//    }
//
//    protected void igniteEnvironment() {
//        Vec3D v1 = Vec3D.createVector(posX, posY, posZ);
//        Vec3D v2 = Vec3D.createVector(posX + motionX, posY + motionY, posZ + motionZ);
//
//        MovingObjectPosition target = worldObj.rayTraceBlocks(v1, v2);
//
//        v1 = Vec3D.createVector(posX, posY, posZ);
//        v2 = Vec3D.createVector(posX + motionX, posY + motionY, posZ + motionZ);
//
//        if (target != null) {
//            v2 = Vec3D.createVector(target.hitVec.xCoord, target.hitVec.yCoord, target.hitVec.zCoord);
//        }
//
//        Entity touchedEntity = null;
//        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this,
//                boundingBox.addCoord(motionX, motionY, motionZ).expand(1, 1, 1));
//        double minDist = 0;
//
//        for (int j = 0; j < list.size(); j++) {
//            Entity ent = (Entity) list.get(j);
//
//            if (!ent.canBeCollidedWith()) {
//                continue;
//            }
//
//            float aabbOfs = 0.3f;
//            AxisAlignedBB aabb = ent.boundingBox.expand(aabbOfs, aabbOfs, aabbOfs);
//            MovingObjectPosition entTarget = aabb.calculateIntercept(v1, v2);
//
//            if (entTarget == null) {
//                continue;
//            }
//
//            double dist = v1.distanceTo(entTarget.hitVec);
//
//            if (dist < minDist || minDist == 0) {
//                touchedEntity = ent;
//                minDist = dist;
//            }
//        }
//
//        if (touchedEntity != null && touchedEntity != owner) {
//            target = new MovingObjectPosition(touchedEntity);
//        }
//
//        if (target != null) {
//            igniteTarget(target);
//        }
//    }
//
//    protected void igniteTarget(MovingObjectPosition target) {
//        if (igniteEntities && target.typeOfHit == EnumMovingObjectType.ENTITY && !target.entityHit.isImmuneToFire()) {
//            if (owner != null) {
//                if (target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, owner), igniteDamage)) {
//                    target.entityHit.setFire(igniteDuration);
//                }
//            } else {
//                if (target.entityHit.attackEntityFrom(DamageSource.onFire, igniteDamage)) {
//                    target.entityHit.setFire(igniteDuration);
//                }
//            }
//        }
//
//        if (igniteBlocks && target.typeOfHit == EnumMovingObjectType.TILE) {
//            int bx = target.blockX;
//            int by = target.blockY;
//            int bz = target.blockZ;
//
//            switch (target.sideHit) {
//                case 0:
//                    by--;
//                    break;
//
//                case 1:
//                    by++;
//                    break;
//
//                case 2:
//                    bz--;
//                    break;
//
//                case 3:
//                    bz++;
//                    break;
//
//                case 4:
//                    bx--;
//                    break;
//
//                case 5:
//                    bx++;
//                    break;
//            }
//
//            if (worldObj.isAirBlock(bx, by, bz)) {
//                worldObj.setBlockWithNotify(bx, by, bz, Block.fire.blockID);
//                if (Block.fire.canBlockCatchFire(worldObj, target.blockX, target.blockY, target.blockZ)) {
//                    worldObj.spawnParticle("lava", bx, by, bz, 0, 0, 0);
//                }
//            }
//        }
//    }

  // change size of entity AABB used for collisions
  // when the entity size is changed, it changes the bounding box but doesn't recentre it, so the xpos and zpos move
  //  (the entity update resetPositionToBB copies it back)
  // To fix this, we resize the AABB around the existing centre
  protected void changeSize(float width, float height)
  {
    if (width != this.width || height != this.height) {
      AxisAlignedBB oldAABB = this.getEntityBoundingBox();
      double oldMidptX = (oldAABB.minX + oldAABB.maxX)/2.0;
      double oldMidptZ = (oldAABB.minZ + oldAABB.maxZ)/2.0;

      this.width = width;
      this.height = height;

      AxisAlignedBB newAABB = new AxisAlignedBB(oldMidptX - width / 2.0, oldAABB.minY, oldMidptZ - width / 2.0,
                                                oldMidptX + width / 2.0, oldAABB.maxY, oldMidptZ + width / 2.0);
      this.setEntityBoundingBox(newAABB);
    }
  }

  protected EnumParticleTypes getSmokeParticleID() {
    if (largeSmokeChance != 0 && rand.nextFloat() <= largeSmokeChance) {
      return EnumParticleTypes.SMOKE_LARGE;
    } else {
      return EnumParticleTypes.SMOKE_NORMAL;
    }
  }


}
