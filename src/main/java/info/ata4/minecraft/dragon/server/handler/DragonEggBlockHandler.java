/*
 ** 2014 January 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.minecraft.dragon.server.handler;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Non-invasive dragon egg block override handler.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonEggBlockHandler {

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent evt) {
        // ignore client events
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            return;
        }
        
        // only handle right clicks on blocks
        if (evt.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        BlockPos pos = evt.pos;
        World world = evt.entity.worldObj;
        Block block = world.getBlockState(evt.pos).getBlock();
        
        // ignore non-egg blocks
        if (block != Blocks.dragon_egg) {
            return;
        }
        
        // deny action
        evt.useBlock = PlayerInteractEvent.Result.DENY;
        evt.useItem = PlayerInteractEvent.Result.DENY;
        
        // clear dragon egg block
        world.destroyBlock(pos, false);
        
        // create dragon egg entity
        EntityTameableDragon dragon = new EntityTameableDragon(world);
        dragon.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        dragon.getReproductionHelper().setBreederName(evt.entityPlayer.getName());
        dragon.getLifeStageHelper().setLifeStage(DragonLifeStage.EGG);
        world.spawnEntityInWorld(dragon);
    }
}
