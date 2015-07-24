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

  protected float particleMaxSize;

  public float largeSmokeChance = 0.3f;

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
  }

  protected EnumParticleTypes getSmokeParticleID() {
    if (largeSmokeChance != 0 && rand.nextFloat() <= largeSmokeChance) {
      return EnumParticleTypes.SMOKE_LARGE;
    } else {
      return EnumParticleTypes.SMOKE_NORMAL;
    }
  }

}
