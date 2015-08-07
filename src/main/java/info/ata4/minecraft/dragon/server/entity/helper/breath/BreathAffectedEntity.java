package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

/**
* Created by TGG on 7/08/2015.
 *  * Models an entity which is being affected by the breath weapon
 * Every tick that an entity is exposed to the breath weapon, its "hit density" increases.

 */
public class BreathAffectedEntity
{
  public BreathAffectedEntity()
  {
    hitDensity = 0.0F;
    timeSinceLastHit = 0;
  }

  /**
   * increases the hit density of the entity
   * @param increase the amount to increase the hit density by
   */
  public void addHitDensity(Vec3 beamDirection, float increase)
  {
    hitDensity += increase;
    timeSinceLastHit = 0;
  }

  public float getHitDensity()
  {
    return hitDensity;
  }

//  public void setHitDensity(EnumFacing face, float newValue)
//  {
//    hitDensity[face.getIndex()] = newValue;
//  }

  private float ENTITY_DECAY_PERCENTAGE_PER_TICK = 10.0F;
  private float ENTITY_RESET_EFFECT_THRESHOLD = 0.01F;
  private final int TICKS_BEFORE_DECAY_STARTS = 10;

  /** updates the breath weapon's effect for a given entity
   *   called every tick; used to decay the cumulative effect on the entity
   *   for example - an entity being gently bathed in flame might gain 0.2 every time from the beam, and lose 0.2 every
   *     tick in this method.
   * @return the new effect density; negative for effect expired
   */
  public void decayEntityEffectTick()
  {
    if (++timeSinceLastHit < TICKS_BEFORE_DECAY_STARTS) return;
    hitDensity *= (1.0F - ENTITY_DECAY_PERCENTAGE_PER_TICK / 100.0F);
    if (hitDensity < ENTITY_RESET_EFFECT_THRESHOLD){
      hitDensity = 0.0F;
    }
  }



  /**
   * Check if this block is unaffected by the breath weapon
   * @return true if the block is currently unaffected
   */
  public boolean isUnaffected()
  {
    return hitDensity >= ENTITY_RESET_EFFECT_THRESHOLD;
  }

  private float hitDensity;
  private int timeSinceLastHit;
}
