package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Created by EveryoneElse on 21/06/2015.
 */
public class BreathWeaponEmitter {

  private Vec3 origin;
  private Vec3 direction;

  private Vec3 previousOrigin;
  private Vec3 previousDirection;

  public void updateFromDragon(EntityTameableDragon dragon)
  {
    origin = dragon.getPositionEyes(1.0F);
    direction = dragon.getLook(1.0F);
  }

  public void spawnBreathParticles(World world)
  {
    if (previousDirection == null) previousDirection = direction;
    if (previousOrigin == null) previousOrigin = origin;
    final int PARTICLES_PER_TICK = 4;
    for (int i = 0; i < PARTICLES_PER_TICK; ++i) {
      float fraction = 1.0F / PARTICLES_PER_TICK;
      float distanceFraction = 1 - fraction;
      Vec3 interpDirection = interpolateVec(previousDirection, direction, fraction);
      Vec3 interpOrigin = interpolateVec(previousOrigin, origin, fraction);
      FlameBreathFX flameBreathFX = new FlameBreathFX(world, interpOrigin.xCoord, interpOrigin.yCoord, interpOrigin.zCoord,
                                                      interpDirection.xCoord, interpDirection.yCoord, interpDirection.zCoord,
                                                      distanceFraction);
      Minecraft.getMinecraft().effectRenderer.addEffect(flameBreathFX);
    }
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
