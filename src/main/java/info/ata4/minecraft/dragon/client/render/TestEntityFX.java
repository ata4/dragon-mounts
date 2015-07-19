package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.util.math.RotatingQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by EveryoneElse on 21/06/2015.
 */
public class TestEntityFX extends EntityFX {
  private final ResourceLocation fireballRL = new ResourceLocation("dragonmounts:entities/breath_fire");
  private Entity owner;

  protected float particleMaxSize;

  public float smokeChance = 0.1f;
  public float largeSmokeChance = 0.3f;
  public boolean igniteBlocks = true;
  public boolean igniteEntities = true;
  public int igniteDamage = 2;
  public int igniteDuration = 5;
  public float igniteChance = 0.12f;

  private final float MAX_ALPHA = 0.99F;
  private final double INITIAL_SPEED = 0.6; // blocks per tick

  public TestEntityFX(World world, double x, double y, double z, double directionX, double directionY, double directionZ,
                      float partialTicksElapsed) {
    this(world, x, y, z, directionX, directionY, directionZ, 4, 40, partialTicksElapsed);
  }

  public TestEntityFX(World world, double x, double y, double z, double directionX, double directionY, double directionZ,
                      float size, int age, float partialTicksElapsed) {
    super(world, x, y, z, directionX, directionY, directionZ);

    Vec3 direction = new Vec3(directionX, directionY, directionZ).normalize();

    final double SPEED_VARIATION_FACTOR = 0.1;
    motionX = direction.xCoord * INITIAL_SPEED * (1 + SPEED_VARIATION_FACTOR * rand.nextGaussian());
    motionY = direction.yCoord * INITIAL_SPEED * (1 + SPEED_VARIATION_FACTOR * rand.nextGaussian());
    motionZ = direction.zCoord * INITIAL_SPEED * (1 + SPEED_VARIATION_FACTOR * rand.nextGaussian());

    posX += motionX * partialTicksElapsed;
    posY += motionY * partialTicksElapsed;
    posZ += motionZ * partialTicksElapsed;
    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;

    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fireballRL.toString());
    func_180435_a(sprite);
    particleGravity = Blocks.fire.blockParticleGravity;  /// arbitrary block!
    particleMaxSize = size + (float)rand.nextGaussian() * (size / 2f);
    particleMaxAge = age + (int) (rand.nextFloat() * (age / 2f));
    this.particleAlpha = MAX_ALPHA;  // a value less than 1 turns on alpha blending
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

  @Override
  public int getBrightnessForRender(float partialTick)
  {
    return 0xf000f0;
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
//    Random random = new Random();
//    if (random.nextBoolean()) {
//      tex.mirrorLR();
//    }
//    tex.rotate90(random.nextInt(4));

    double scale = 0.1F * this.particleScale;
    double x = this.prevPosX + (this.posX - this.prevPosX) * partialTick - interpPosX;
    double y = this.prevPosY + (this.posY - this.prevPosY) * partialTick - interpPosY;
    double z = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTick - interpPosZ;
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
    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;

    int cyclePos = particleAge % 200;
    if (cyclePos < 100) {
      particleScale = cyclePos / 100.0F;
    } else {
      particleScale = 2 - cyclePos / 100.0F;
    }
    particleScale = 0.1F + particleMaxSize * particleScale;
    setSize(0.5f * particleScale, 0.5f * particleScale);
    ++particleAge;
//
//    float lifetimeFraction = (float) particleAge / (float) particleMaxAge;
//    lifetimeFraction = MathHelper.clamp_float(lifetimeFraction, 0.0F, 1.0F);
//
//    final float YOUNG_AGE = 0.25F;
//    final float OLD_AGE = 0.75F;
//
//    if (lifetimeFraction < YOUNG_AGE) {
//      particleScale = 1 + MathHelper.sin(lifetimeFraction / YOUNG_AGE * (float) Math.PI / 2.0F) * particleMaxSize;
//      particleAlpha = MAX_ALPHA;
//    } else if (lifetimeFraction < OLD_AGE) {
//      particleScale = 1 + particleMaxSize;
//      particleAlpha = MAX_ALPHA;
//    } else {
//      particleScale = 1 + particleMaxSize;
//      particleAlpha = MAX_ALPHA * (1 - lifetimeFraction);
//    }
//
//    setSize(0.5f * particleScale, 0.5f * particleScale);
////    yOffset = height / 2f;  todo still necessary?
//
//    // spawn a smoke trail after some time
//    if (smokeChance != 0 && rand.nextFloat() < lifetimeFraction && rand.nextFloat() <= smokeChance) {
//      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, motionX * 0.5, motionY * 0.5, motionZ * 0.5);
//    }
//
//    if (particleAge++ >= particleMaxAge) {
//      setDead();
//      return;
//    }
//
//    // extinguish when hitting water
//    if (handleWaterMovement()) {
//      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, 0, 0, 0);
//      setDead();
//      return;
//    }
//
////        motionY += 0.02;   what's this for?  why rise?
//
//    moveEntity(motionX, motionY, motionZ);
//
////        if (posY == prevPosY) {
////            motionX *= 1.1;
////            motionZ *= 1.1;
////        }
//
////    final double SPEED_LOSS_PERCENT_PER_SECOND = 20;
////    final double SPEED_MULT_PER_TICK = 1.0 - SPEED_LOSS_PERCENT_PER_SECOND / 100.0 / 20.0;
////    motionX *= SPEED_MULT_PER_TICK;
////    motionY *= SPEED_MULT_PER_TICK;
////    motionZ *= SPEED_MULT_PER_TICK;
////
////    if (onGround) {
////      motionX *= 0.7;
////      motionZ *= 0.7;
////    }
//
//    // collision ages particles faster
//    if (isCollided) {
//      particleAge += 5;
//    }
//
//    // slow particles age very fast
//    final double SPEED_THRESHOLD = INITIAL_SPEED * 0.25;
//    if (motionX * motionX + motionY * motionY + motionZ * motionZ < SPEED_THRESHOLD * SPEED_THRESHOLD) {
//      particleAge += 20;
//    }
//
////        // ignite environment
////        if ((igniteEntities || igniteBlocks) && rand.nextFloat() <= igniteChance) {
////            igniteEnvironment();
////        }
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

  protected EnumParticleTypes getSmokeParticleID() {
    if (largeSmokeChance != 0 && rand.nextFloat() <= largeSmokeChance) {
      return EnumParticleTypes.SMOKE_LARGE;
    } else {
      return EnumParticleTypes.SMOKE_NORMAL;
    }
  }


}