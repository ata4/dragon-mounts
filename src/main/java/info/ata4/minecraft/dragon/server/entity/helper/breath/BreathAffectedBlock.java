package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.util.EnumFacing;

/**
* Created by TGG on 7/08/2015.
 * Models a block which is being affected by the breath weapon
 * Every tick that a block is exposed to the breath weapon, its "hit density" increases.
*/
public class BreathAffectedBlock
{
  public BreathAffectedBlock()
  {
    hitDensity = new float[EnumFacing.values().length];
    timeSinceLastHit = 0;
  }

  /**
   * increases the hit density of the specified face.
   * @param face the face being hit; null = no particular face
   * @param increase the amount to increase the hit density by
   */
  public void addHitDensity(EnumFacing face, float increase)
  {
    if (face == null) {
      increase /= EnumFacing.values().length;
      for (EnumFacing facing : EnumFacing.values()) {
        hitDensity[facing.getIndex()] += increase;
      }
    } else {
      hitDensity[face.getIndex()] += increase;
    }
    timeSinceLastHit = 0;
  }

  public float getHitDensity(EnumFacing face)
  {
    return hitDensity[face.getIndex()];
  }

  public float getMaxHitDensity() {
    float maxDensity = 0;
    for (EnumFacing facing : EnumFacing.values()) {
      maxDensity = Math.max(maxDensity, hitDensity[facing.getIndex()]);
    }
    return maxDensity;
  }

//  public void setHitDensity(EnumFacing face, float newValue)
//  {
//    hitDensity[face.getIndex()] = newValue;
//  }

  private final float BLOCK_DECAY_PERCENTAGE_PER_TICK = 10.0F;
  private final float BLOCK_RESET_EFFECT_THRESHOLD = 0.0001F;  //todo change back to 0.01F
  private final int TICKS_BEFORE_DECAY_STARTS = 10000; // todo change back to 10

  /** updates the breath weapon's effect for a given block
   *   called every tick; used to decay the cumulative effect on the block
   *   for example - a block being gently bathed in flame might gain 0.2 every time from the beam, and lose 0.2 every
   *     tick in this method.
   */
  public void decayBlockEffectTick()
  {
    if (++timeSinceLastHit < TICKS_BEFORE_DECAY_STARTS) return;

    for (EnumFacing facing : EnumFacing.values()) {
      final float EXPIRED_VALUE = 0.0F;
      float density = hitDensity[facing.getIndex()];
      density *= (1.0F - BLOCK_DECAY_PERCENTAGE_PER_TICK / 100.0F);
      density = (density < BLOCK_RESET_EFFECT_THRESHOLD) ? EXPIRED_VALUE : density;
      hitDensity[facing.getIndex()] = density;
    }
  }

  /**
   * Check if this block is unaffected by the breath weapon
   * @return true if the block is currently unaffected
   */
  public boolean isUnaffected()
  {
    for (EnumFacing facing : EnumFacing.values()) {
      if (hitDensity[facing.getIndex()] > BLOCK_RESET_EFFECT_THRESHOLD) return false;
    }
    return true;
  }

  private float [] hitDensity;
  private int timeSinceLastHit;
}
