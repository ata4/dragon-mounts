package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.util.Vec3;

/**
* Created by TGG on 7/08/2015.
 *  * Models an entity which is being affected by the breath weapon
 * Every tick that an entity is exposed to the breath weapon, its "hit density" increases.
 *  Keeps track of the number of ticks that the entity has been exposed to the breath.
 *  Typical usage:
 *  1) Create a new BreathAffectedEntity for the entity
 *  2) Each time it is hit, call addHitDensity() proportional to the exposure + strength of the weapon
 *  3) Every tick, call decayEntityEffectTick
 *
 *  Query the entity's damage by
 *  1) getHitDensity
 *  2) isUnaffected
 *  3) each tick: if applyDamageThisTick() is true, apply the weapon damage now.  (This is used to space out the
 *     damage so that armour doesn't protect so much (eg 20 damage delivered once per second instead of 1 damage
 *     delivered twenty times per second - (a player with armour is invulnerable to that)  )
 */
public class BreathAffectedEntity
{
  public BreathAffectedEntity()
  {
    hitDensity = 0.0F;
    timeSinceLastHit = 0;
    ticksUntilDamageApplied = TICKS_BETWEEN_DAMAGE_APPLICATION;
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

  /**
   *  returns true if damage should be applied this tick
   *  @return true if damage should be applied.  Resets after the call (repeated calls return false)
   */
  public boolean applyDamageThisTick()
  {
    if (ticksUntilDamageApplied > 0) return false;
    ticksUntilDamageApplied = TICKS_BETWEEN_DAMAGE_APPLICATION;
    return true;
  }

  private float ENTITY_DECAY_PERCENTAGE_PER_TICK = 5.0F;
  private float ENTITY_RESET_EFFECT_THRESHOLD = 0.01F;
  private final int TICKS_BEFORE_DECAY_STARTS = 40;
  private final int TICKS_BETWEEN_DAMAGE_APPLICATION = 20;  // apply damage every x ticks

  /** updates the breath weapon's effect for a given entity
   *   called every tick; used to decay the cumulative effect on the entity
   *   for example - an entity being gently bathed in flame might gain 0.2 every time from the beam, and lose 0.2 every
   *     tick in this method.
   */
  public void decayEntityEffectTick()
  {
    if (timeSinceLastHit == 0 && ticksUntilDamageApplied > 0) {
      --ticksUntilDamageApplied;
    }
    if (++timeSinceLastHit < TICKS_BEFORE_DECAY_STARTS) return;
    hitDensity *= (1.0F - ENTITY_DECAY_PERCENTAGE_PER_TICK / 100.0F);
    if (hitDensity < ENTITY_RESET_EFFECT_THRESHOLD){
      hitDensity = 0.0F;
    }
    ++ticksUntilDamageApplied;
    ticksUntilDamageApplied = Math.min(ticksUntilDamageApplied, TICKS_BETWEEN_DAMAGE_APPLICATION);
  }

  /**
   * Check if this block is unaffected by the breath weapon
   * @return true if the block is currently unaffected
   */
  public boolean isUnaffected()
  {
    return hitDensity < ENTITY_RESET_EFFECT_THRESHOLD;
  }

  private float hitDensity;
  private int timeSinceLastHit;
  private int ticksUntilDamageApplied;
}
