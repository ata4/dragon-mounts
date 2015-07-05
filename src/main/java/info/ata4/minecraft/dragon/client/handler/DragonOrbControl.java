/*
 ** 2013 October 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.handler;

        import info.ata4.minecraft.dragon.DragonMounts;
        import info.ata4.minecraft.dragon.server.CommonProxy;
        import info.ata4.minecraft.dragon.server.ItemDragonOrb;
        import info.ata4.minecraft.dragon.server.network.DragonControlMessage;
        import info.ata4.minecraft.dragon.server.util.ItemUtils;
        import net.minecraft.client.Minecraft;
        import net.minecraft.client.entity.EntityPlayerSP;
        import net.minecraft.client.settings.KeyBinding;
        import net.minecraft.item.ItemStack;
        import net.minecraft.util.MovingObjectPosition;
        import net.minecraftforge.fml.client.registry.ClientRegistry;
        import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
        import net.minecraftforge.fml.common.gameevent.TickEvent;
        import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
        import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
        import org.lwjgl.input.Keyboard;

        import java.util.BitSet;

/**
 * If the player is holding the dragon orb, records whether the player is holding down the
 *   trigger and what the current target is (where the player is pointing the cursor)
 */
public class DragonOrbControl {

  public DragonOrbControl getInstance() {
    if (instance == null) {
      instance = new DragonOrbControl();
    }
    return instance;
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent evt) {
    if (evt.phase != ClientTickEvent.Phase.START) return;
    EntityPlayerSP entityPlayerSP = Minecraft.getMinecraft().thePlayer;
    if (entityPlayerSP == null) return;
    if (!ItemUtils.hasEquipped(entityPlayerSP, DragonMounts.proxy.itemDragonOrb));
    triggerHeld = entityPlayerSP.isUsingItem();
    if (triggerHeld) {

    }
  }

  private boolean triggerHeld = false;
  private MovingObjectPosition movingObjectPosition;

  private DragonOrbControl instance = null;

  private DragonOrbControl() {
  }
}
