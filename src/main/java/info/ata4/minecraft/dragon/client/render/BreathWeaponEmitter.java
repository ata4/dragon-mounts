package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.helper.breath.BreathNode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by TGG on 21/06/2015.
 */
public class BreathWeaponEmitter {

  private Vec3 origin;
  private Vec3 direction;

  private Vec3 previousOrigin;
  private Vec3 previousDirection;
  private int previousTickCount;

  public void setBeamEndpoints(Vec3 newOrigin, Vec3 newDestination)
  {
    origin = newOrigin;
    direction = newDestination.subtract(newOrigin).normalize();
  }

  private static boolean spawnedOne = false;
  static private int spawnSkip = 0;

  /**
   * Spawn breath particles for this tick.  If the beam endpoints have moved, interpolate between them, unless
   *   the beam stopped for a while (tickCount skipped one or more tick)
   * @param world
   * @param power the strength of the beam
   * @param tickCount
   */
  public void spawnBreathParticles(World world, BreathNode.Power power, int tickCount)
  {
//    if (!spawnedOne) {  //todo remove : spawns a test FX at a fixed offset from the player
//      spawnedOne = true;
//      FlameBreathFXTest flameBreathFX = FlameBreathFXTest.createFlameBreathFXTest(world,
//                                                                      origin.xCoord, origin.yCoord, origin.zCoord,
//                                                                      direction.xCoord, direction.yCoord, direction.zCoord,
//                                                                      power,
//                                                                      0);
//      Minecraft.getMinecraft().effectRenderer.addEffect(flameBreathFX);
//    }

//    if (spawnedOne) return;
//    TestEntityFX testEntityFX = new TestEntityFX(world, origin.xCoord, origin.yCoord + 5, origin.zCoord,
//            0, 0, 0, 1.0F);
//    Minecraft.getMinecraft().effectRenderer.addEffect(testEntityFX);
//    spawnedOne = true;
//    return;

    if (tickCount - previousTickCount > 1) {
      previousDirection = direction;
      previousOrigin = origin;
    } else {
      if (previousDirection == null) previousDirection = direction;
      if (previousOrigin == null) previousOrigin = origin;
    }
    final int PARTICLES_PER_TICK = 4;
    for (int i = 0; i < PARTICLES_PER_TICK; ++i) {
//      if (++spawnSkip < 399) continue;
      spawnSkip = 0;
      float partialTickHeadStart = i / (float)PARTICLES_PER_TICK;
      Vec3 interpDirection = interpolateVec(previousDirection, direction, partialTickHeadStart);
      Vec3 interpOrigin = interpolateVec(previousOrigin, origin, partialTickHeadStart);
      FlameBreathFX flameBreathFX = FlameBreathFX.createFlameBreathFX(world,
              interpOrigin.xCoord, interpOrigin.yCoord, interpOrigin.zCoord,
              interpDirection.xCoord, interpDirection.yCoord, interpDirection.zCoord,
              power,
              partialTickHeadStart);
      Minecraft.getMinecraft().effectRenderer.addEffect(flameBreathFX);
    }
    previousDirection = direction;
    previousOrigin = origin;
  }

  /**
   * interpolate from vector 1 to vector 2 using fraction
   * @param vector1
   * @param vector2
   * @param fraction 0 - 1; 0 = vector1, 1 = vector2
   * @return interpolated vector
   */
  private Vec3 interpolateVec(Vec3 vector1, Vec3 vector2, float fraction)
  {
    return new Vec3(vector1.xCoord * (1-fraction) + vector2.xCoord * fraction,
                    vector1.yCoord * (1-fraction) + vector2.yCoord * fraction,
                    vector1.zCoord * (1-fraction) + vector2.zCoord * fraction
                    );
  }
}
