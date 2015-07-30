package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
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
 * Created by TGG on 21/06/2015.
 */
public class FlameBreathFX extends EntityFX {
  private final ResourceLocation fireballRL = new ResourceLocation("dragonmounts:entities/breath_fire");

//  protected float currentParticleSize;
//  protected float particleMaxSize;

  public float smokeChance = 0.1f;
  public float largeSmokeChance = 0.3f;

  private static final float MAX_ALPHA = 0.99F;
  private static final float AABB_RELATIVE_TO_SIZE = 0.5F;  // how big is the AABB relative to the fireball size.

  private static final double SPEED_VARIATION_ABS = 0.1;  // plus or minus this amount (3 std deviations)
  private static final double AGE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)
  private static final double SIZE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)

  private BreathNode breathNode;

  public static FlameBreathFX createFlameBreathFX(World world, double x, double y, double z,
                                                  double directionX, double directionY, double directionZ,
                                                  BreathNode.Power power,
                                                  float partialTicksHeadStart)
  {
    Vec3 direction = new Vec3(directionX, directionY, directionZ).normalize();

    BreathNode newNode = new BreathNode(power);
    float initialSpeed = newNode.getStartingSpeed();

    Random rand = new Random();
    double actualMotionX = direction.xCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    double actualMotionY = direction.yCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    double actualMotionZ = direction.zCoord + MathX.getTruncatedGaussian(rand, 0, SPEED_VARIATION_ABS);
    actualMotionX *= initialSpeed;
    actualMotionY *= initialSpeed;
    actualMotionZ *= initialSpeed;

    x += actualMotionX * partialTicksHeadStart;
    y += actualMotionY * partialTicksHeadStart;
    z += actualMotionZ * partialTicksHeadStart;
    return new FlameBreathFX(world, x, y, z, actualMotionX, actualMotionY, actualMotionZ,
                             newNode);
  }

  private FlameBreathFX(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                        BreathNode i_breathNode) {
    super(world, x, y, z, velocityX, velocityY, velocityZ);

    breathNode = i_breathNode;
    particleGravity = Blocks.fire.blockParticleGravity;  /// arbitrary block!  maybe not even required.
    breathNode.setRelativeLifetime(MathX.getTruncatedGaussian(rand, 1, AGE_VARIATION_FACTOR));
    breathNode.setRelativeSize(MathX.getTruncatedGaussian(rand, 1, SIZE_VARIATION_FACTOR));

    particleMaxAge = (int)breathNode.getMaxLifeTime();
    this.particleAlpha = MAX_ALPHA;  // a value less than 1 turns on alpha blending
//    currentParticleSize = getParticleSize(0.0F);

    float initialCollisionSize = AABB_RELATIVE_TO_SIZE * breathNode.getSize();
    changeSize(initialCollisionSize, initialCollisionSize);  // using setSize causes trouble.

    //undo random velocity variation of vanilla constructor
    motionX = velocityX;
    motionY = velocityY;
    motionZ = velocityZ;

    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fireballRL.toString());
    func_180435_a(sprite);
  }

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

//  private float getParticleSize(float fractionOfFullSize)
//  {
//
//  }

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
//    float lifetimeFraction = (float) particleAge / (float) particleMaxAge;
//    lifetimeFraction = MathHelper.clamp_float(lifetimeFraction, 0.0F, 1.0F);

    final float YOUNG_AGE = 0.25F;
    final float OLD_AGE = 0.75F;

    float lifetimeFraction = breathNode.getLifetimeFraction();
    if (lifetimeFraction < YOUNG_AGE) {
//      float fractionalSize = MathHelper.sin(lifetimeFraction / YOUNG_AGE * (float) Math.PI / 2.0F);
//      currentParticleSize = getParticleSize(fractionalSize);
      particleAlpha = MAX_ALPHA;
    } else if (lifetimeFraction < OLD_AGE) {
//      currentParticleSize = getParticleSize(1.0F);
      particleAlpha = MAX_ALPHA;
    } else {
//      currentParticleSize = getParticleSize(1.0F);
      particleAlpha = MAX_ALPHA * (1 - lifetimeFraction);
    }

    float currentParticleSize = breathNode.getSize();
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

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    moveEntity(motionX, motionY, motionZ);

    // collision ages particles faster
    if (isCollided) {
      particleAge += 5;
      if (onGround) {
        motionY -= 0.01F; // ensure that we hit the ground next time too
      }
    }

    // slow particles age very fast (they look silly when sitting still)
    final double SPEED_THRESHOLD = breathNode.getStartingSpeed() * 0.25;
    if (motionX * motionX + motionY * motionY + motionZ * motionZ < SPEED_THRESHOLD * SPEED_THRESHOLD) {
      particleAge += 20;
    }

    breathNode.setAgeTicks(particleAge);
  }

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
