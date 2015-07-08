package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.client.handler.DragonOrbControl;
import info.ata4.minecraft.dragon.client.render.BreathWeaponEmitter;
import info.ata4.minecraft.dragon.client.render.FlameBreathFX;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.network.DragonOrbTarget;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

/**
 * Created by TGG on 8/07/2015.
 */
public class DragonBreathHelper extends DragonHelper
{
  public DragonBreathHelper(EntityTameableDragon dragon, int dataWatcherIndexBreathTarget)
  {
    super(dragon);
    DATA_WATCHER_BREATH_TARGET = dataWatcherIndexBreathTarget;
    dataWatcher.addObject(DATA_WATCHER_BREATH_TARGET, "");
    if (dragon.isClient()) {
      breathWeaponEmitter = new BreathWeaponEmitter();
    }
  }

  private final int DATA_WATCHER_BREATH_TARGET;

  private DragonOrbTarget desiredRangedTarget = null; // the target of the breath weapon; null = no target
  private DragonOrbTarget lastTargetSent = null;
  private BreathWeaponEmitter breathWeaponEmitter = null;
  private int tickCounter = 0;

  /**
   * sets the desired target for the breath weapon.  Will be used by the targeting AI to determine where to point
   *   the breath weapon
   * @param newDesiredRangedTarget where the dragon should breathe at. null = no target
   */
  public void setDesiredRangedTarget(DragonOrbTarget newDesiredRangedTarget)
  {
    if (newDesiredRangedTarget == null) {
      clearTarget();
      return;
    }
    if (dragon.isClient()) {
      desiredRangedTarget = newDesiredRangedTarget;
      return;
    }
    desiredRangedTarget = newDesiredRangedTarget;
    boolean updateDataWatcher = false;
    if (lastTargetSent == null) {
      updateDataWatcher = true;
    } else {
      updateDataWatcher = !newDesiredRangedTarget.approximatelyMatches(lastTargetSent);
    }
    if (updateDataWatcher) {
      dataWatcher.updateObject(DATA_WATCHER_BREATH_TARGET, desiredRangedTarget.toEncodedString());
    }
  }

  /**
   * clears the target for the breath weapon, i.e. no target
   */
  public void clearTarget()
  {
    desiredRangedTarget = null;
    if (dragon.isClient() || lastTargetSent == null) return;
    dataWatcher.updateObject(DATA_WATCHER_BREATH_TARGET, "");
  }

  @Override
  public void onLivingUpdate() {
    ++tickCounter;

    // for testing only
    // todo update

    // update target
    if (dragon.isClient()) {
      Entity entityPlayer = DragonMounts.proxy.getClientEntityPlayerSP();   // on client, grab target info from local
      if (entityPlayer != null && entityPlayer == dragon.getOwner()) {
        setDesiredRangedTarget(DragonOrbControl.getInstance().getTarget());
      }
    }

    if (dragon.isClient() && desiredRangedTarget != null) {
      Vec3 origin = dragon.getDragonHeadPositionHelper().getThroatPosition();
      Vec3 destination;
      switch (desiredRangedTarget.getTypeOfTarget()) {
        case LOCATION: {
          destination = desiredRangedTarget.getTargetedLocation();
          break;
        }
        case DIRECTION: {
          destination = origin.add(desiredRangedTarget.getTargetedDirection());
          break;
        }
        case ENTITY: {
          Entity entity = desiredRangedTarget.getTargetEntity(dragon.worldObj);
          if (entity == null) {
            destination = null;
          } else {
            destination = entity.getPositionVector();
          }
          break;
        }
        default: {
          System.err.println("Unexpected target type:" + desiredRangedTarget.getTypeOfTarget());
          destination = null;
          break;
        }
      }
      if (destination != null) {
        breathWeaponEmitter.setBeamEndpoints(origin, destination);
        FlameBreathFX.Power power = dragon.getLifeStageHelper().getBreathPower();
        breathWeaponEmitter.spawnBreathParticles(dragon.getEntityWorld(), power, tickCounter);
      }
    }
  }

  @Override
  public void writeToNBT(NBTTagCompound nbt) {}
  @Override
  public void readFromNBT(NBTTagCompound nbt) {}
  @Override
  public void applyEntityAttributes() {}
  @Override
  public void onDeathUpdate() {}
  @Override
  public void onDeath() {}

}
