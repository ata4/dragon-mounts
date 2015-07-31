package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
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

  public float smokeChance = 0.1f;
  public float largeSmokeChance = 0.3f;

  private static final float MAX_ALPHA = 0.99F;

  private BreathNode breathNode;

  public static FlameBreathFX createFlameBreathFX(World world, double x, double y, double z,
                                                  double directionX, double directionY, double directionZ,
                                                  BreathNode.Power power,
                                                  float partialTicksHeadStart)
  {
    Vec3 direction = new Vec3(directionX, directionY, directionZ).normalize();

    Random rand = new Random();
    BreathNode breathNode = new BreathNode(power);
    breathNode.randomiseProperties(rand);
    Vec3 actualMotion = breathNode.getRandomisedStartingMotion(direction, rand);

    x += actualMotion.xCoord * partialTicksHeadStart;
    y += actualMotion.yCoord * partialTicksHeadStart;
    z += actualMotion.zCoord * partialTicksHeadStart;
    FlameBreathFX newFlameBreathFX = new FlameBreathFX(world, x, y, z, actualMotion, breathNode);
    breathNode.changeEntitySizeToMatch(newFlameBreathFX);
    return newFlameBreathFX;
  }

  private FlameBreathFX(World world, double x, double y, double z, Vec3 motion,
                        BreathNode i_breathNode) {
    super(world, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord);

    breathNode = i_breathNode;
    particleGravity = Blocks.fire.blockParticleGravity;  /// arbitrary block!  maybe not even required.
    particleMaxAge = (int)breathNode.getMaxLifeTime(); // not used, but good for debugging
    this.particleAlpha = MAX_ALPHA;  // a value less than 1 turns on alpha blending

    //undo random velocity variation of vanilla constructor
    motionX = motion.xCoord;
    motionY = motion.yCoord;
    motionZ = motion.zCoord;

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
    final float YOUNG_AGE = 0.25F;
    final float OLD_AGE = 0.75F;

    float lifetimeFraction = breathNode.getLifetimeFraction();
    if (lifetimeFraction < YOUNG_AGE) {
      particleAlpha = MAX_ALPHA;
    } else if (lifetimeFraction < OLD_AGE) {
      particleAlpha = MAX_ALPHA;
    } else {
      particleAlpha = MAX_ALPHA * (1 - lifetimeFraction);
    }

    final float PARTICLE_SCALE_RELATIVE_TO_SIZE = 5.0F; // factor to convert from particleSize to particleScale
    float currentParticleSize = breathNode.getCurrentSize();
    particleScale = PARTICLE_SCALE_RELATIVE_TO_SIZE * currentParticleSize;

    breathNode.changeEntitySizeToMatch(this); // note - will change posX, posY, posZ to keep centre constant when resizing

    // spawn a smoke trail after some time
    if (smokeChance != 0 && rand.nextFloat() < lifetimeFraction && rand.nextFloat() <= smokeChance) {
      worldObj.spawnParticle(getSmokeParticleID(), posX, posY, posZ, motionX * 0.5, motionY * 0.5, motionZ * 0.5);
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

    if (isCollided && onGround) {
        motionY -= 0.01F;         // ensure that we hit the ground next time too
    }
    breathNode.updateAge(this);
    particleAge = (int)breathNode.getAgeTicks();  // not used, but good for debugging
    if (breathNode.isDead()) {
      setDead();
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
