package info.ata4.minecraft.dragon.server.network;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Hashtable;
import java.util.UUID;

/**
 * Created by TGG on 6/07/2015.
 * This class is used to keep track of where each player is actively targeting with their dragon orb
 * Can safely be accessed by both server and network threads
 */
public class DragonOrbTargets
{
  public void setPlayerTarget(EntityPlayerMP player, BreathWeaponTarget target)
  {
    currentPlayerTargets.put(player.getUniqueID(), target);
  }

  /**
   * sets the target for the given player to "no target"
   * @param player
   */
  public void clearPlayerTarget(EntityPlayerMP player)
  {
    currentPlayerTargets.remove(player.getUniqueID());
  }

  /** gets the current target for the given player, or null if no target
   * @param player
   * @return the current target for the given player, or null if no target
   */
  public BreathWeaponTarget getPlayerTarget(EntityPlayerMP player)
  {
    return currentPlayerTargets.get(player.getUniqueID());
  }

  public static synchronized DragonOrbTargets getInstance() {
      if (instance == null) {
        instance = new DragonOrbTargets();
      }
      return instance;
  }

  private DragonOrbTargets() {
    currentPlayerTargets = new Hashtable<UUID, BreathWeaponTarget>();
  }

  // synchronised access by server or network threads
  private Hashtable<UUID, BreathWeaponTarget> currentPlayerTargets;

  private static DragonOrbTargets instance;

}
