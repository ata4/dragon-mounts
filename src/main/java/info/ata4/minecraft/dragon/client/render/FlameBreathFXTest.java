package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import info.ata4.minecraft.dragon.util.math.RotatingQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Random;

public class FlameBreathFXTest extends EntityFX {
  private final ResourceLocation fireballRL = new ResourceLocation("dragonmounts:entities/breath_fidfre"); //breath_fire

  public float smokeChance = 0.1f;
  public float largeSmokeChance = 0.3f;

  private static final float MAX_ALPHA = 0.99F;

//  private BreathNode breathNode;

  public static FlameBreathFXTest createFlameBreathFXTest(World world, double x, double y, double z,
                                                  double directionX, double directionY, double directionZ,
                                                  BreathNode.Power power,
                                                  float partialTicksHeadStart)
  {

    Vec3 actualMotion = new Vec3(0,0,0);

    x += actualMotion.xCoord * partialTicksHeadStart;
    y += actualMotion.yCoord * partialTicksHeadStart;
    z += actualMotion.zCoord * partialTicksHeadStart;
    FlameBreathFXTest newFlameBreathFX = new FlameBreathFXTest(world, x, y, z, actualMotion);
    changeEntitySizeToMatch(newFlameBreathFX);
    return newFlameBreathFX;
  }

  private FlameBreathFXTest(World world, double x, double y, double z, Vec3 motion) {
    super(world, x, y, z, motion.xCoord, motion.yCoord, motion.zCoord);

    particleGravity = Blocks.fire.blockParticleGravity;  /// arbitrary block!  maybe not even required.
//    particleMaxAge = (int)breathNode.getMaxLifeTime(); // not used, but good for debugging
    this.particleAlpha = MAX_ALPHA;  // a value less than 1 turns on alpha blending

    //undo random velocity variation of vanilla EntityFX constructor
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

  /**
   * Render the EntityFX onto the screen.
   * The EntityFX is rendered as a two-dimensional object (Quad) in the world (three-dimensional coordinates).
   *   The corners of the quad are chosen so that the EntityFX is drawn directly facing the viewer (or in other words,
   *   so that the quad is always directly face-on to the screen.)
   * In order to manage this, it needs to know two direction vectors:
   * 1) the 3D vector direction corresponding to left-right on the viewer's screen (edgeLRdirection)
   * 2) the 3D vector direction corresponding to up-down on the viewer's screen (edgeURdirection)
   * These two vectors are calculated by the caller.
   * For example, the top right corner of the quad on the viewer's screen is equal to the centre point of the quad (x,y,z)
   *   plus the edgeLRdirection vector multiplied by half the quad's width, plus the edgeURdirection vector multiplied
   *   by half the quad's height.
   * NB edgeLRdirectionY is not provided because it's always 0, i.e. the top of the viewer's screen is always directly
   *    up so moving left-right on the viewer's screen doesn't affect the y coordinate position in the world
   * @param worldRenderer
   * @param entity
   * @param partialTick
   * @param edgeLRdirectionX edgeLRdirection[XYZ] is the vector direction pointing left-right on the player's screen
   * @param edgeUDdirectionY edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   * @param edgeLRdirectionZ edgeLRdirection[XYZ] is the vector direction pointing left-right on the player's screen
   * @param edgeUDdirectionX edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   * @param edgeUDdirectionZ edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   */

  @Override
  public void func_180434_a(WorldRenderer worldRenderer, Entity entity, float partialTick,
                            float edgeLRdirectionX, float edgeUDdirectionY, float edgeLRdirectionZ,
                            float edgeUDdirectionX, float edgeUDdirectionZ)
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
    final double scaleLR = scale;
    final double scaleUD = scale;
    double x = this.prevPosX + (this.posX - this.prevPosX) * partialTick - interpPosX;
    double y = this.prevPosY + (this.posY - this.prevPosY) * partialTick - interpPosY + this.height / 2.0F;
                                                                // centre of rendering is now y midpt not ymin
    double z = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTick - interpPosZ;

    worldRenderer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
    worldRenderer.addVertexWithUV(x - edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
                                  y - edgeUDdirectionY * scaleUD,
                                  z - edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD,
                                  tex.getU(0),  tex.getV(0));
    worldRenderer.addVertexWithUV(x - edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
                                  y + edgeUDdirectionY * scaleUD,
                                  z - edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD,
                                  tex.getU(1),  tex.getV(1));
    worldRenderer.addVertexWithUV(x + edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
                                  y + edgeUDdirectionY * scaleUD,
                                  z + edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD,
                                  tex.getU(2),  tex.getV(2));
    worldRenderer.addVertexWithUV(x + edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
                                  y - edgeUDdirectionY * scaleUD,
                                  z + edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD,
                                  tex.getU(3),  tex.getV(3));
  }

  private final float NODE_SIZE = 1.0F;

  @Override
  public void onUpdate() {

    particleAlpha = MAX_ALPHA;

    EntityPlayerSP playerSP = Minecraft.getMinecraft().thePlayer;
    final float X_OFFSET = 1.0F;
    final float Y_OFFSET = 1.0F;
    final float Z_OFFSET = 0.0F;

    motionX = playerSP.posX + X_OFFSET- posX ;
    motionY = playerSP.posY + Y_OFFSET - posY;
    motionZ = playerSP.posZ + Z_OFFSET- posZ;

    final float PARTICLE_SCALE_RELATIVE_TO_SIZE = 5.0F; // factor to convert from particleSize to particleScale
    float currentParticleSize = NODE_SIZE;
    particleScale = PARTICLE_SCALE_RELATIVE_TO_SIZE * currentParticleSize;

    System.out.format("before:[%.2f, %.2f, %.2f],", posX, posY, posZ);
    changeEntitySizeToMatch(this); // note - will change posX, posY, posZ to keep centre constant when resizing
    System.out.format("size:[%.2f, %.2f, %.2f],", posX, posY, posZ);

    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;
    moveEntity(motionX, motionY, motionZ);
    System.out.format("after:[%.2f, %.2f, %.2f]\n", posX, posY, posZ);

    if (isCollided && onGround) {
      motionY -= 0.01F;         // ensure that we hit the ground next time too
    }
  }


  /**  copied from BreathNode
   * Change the size of the associated entity to match the node size
   * @param entity
   */
  static public void changeEntitySizeToMatch(Entity entity)
  {
    // change size of entity AABB used for collisions
    // when the entity size is changed, it changes the bounding box but doesn't recentre it, so the xpos and zpos move
    //  (the entity update resetPositionToBB copies it back)
    // To fix this, we resize the AABB around the existing centre

    final float AABB_RELATIVE_TO_SIZE = 1.0F;  // how big is the AABB relative to the fireball size.

    float currentNodeSize = 1.0F;
    float width = (currentNodeSize * AABB_RELATIVE_TO_SIZE);
    float height = (currentNodeSize * AABB_RELATIVE_TO_SIZE);

    if (width != entity.width || height != entity.height) {
      AxisAlignedBB oldAABB = entity.getEntityBoundingBox();
      double oldMidptX = (oldAABB.minX + oldAABB.maxX)/2.0;
      double oldMidptY = (oldAABB.minY + oldAABB.maxY)/2.0;
      double oldMidptZ = (oldAABB.minZ + oldAABB.maxZ)/2.0;

      entity.width = width;
      entity.height = height;

      AxisAlignedBB newAABB = new AxisAlignedBB(oldMidptX - width / 2.0, oldMidptY - height / 2.0, oldMidptZ - width / 2.0,
                                                oldMidptX + width / 2.0, oldMidptY + height / 2.0, oldMidptZ + width / 2.0);
      entity.setEntityBoundingBox(newAABB);
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

