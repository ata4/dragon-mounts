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

import info.ata4.minecraft.dragon.server.block.BlockDragonBreedEgg;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.EnumDragonLifeStage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Non-invasive dragon egg block override handler.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonEggBlockHandler {

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent evt) {
        // only handle right clicks on blocks
        if (evt.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        BlockPos pos = evt.pos;
        World world = evt.entity.worldObj;
        IBlockState state = world.getBlockState(pos);
        Block block = world.getBlockState(pos).getBlock();
        
        // ignore non-egg blocks
        if (block != Blocks.dragon_egg && block != BlockDragonBreedEgg.INSTANCE) {
            return;
        }
        
        // deny action
        evt.useBlock = PlayerInteractEvent.Result.DENY;
        evt.useItem = PlayerInteractEvent.Result.DENY;
        
        // clear dragon egg block
        world.setBlockToAir(pos);

        // create dragon egg entity on server
        if (!evt.world.isRemote) { // this was inverted, i.e. evt.world.isRemote, but it should surely be this way
            EntityTameableDragon dragon = new EntityTameableDragon(world);
            dragon.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            dragon.getReproductionHelper().setBreederName(evt.entityPlayer.getName());
            dragon.getLifeStageHelper().setLifeStage(EnumDragonLifeStage.EGG);
            
            // set breed type (custom dragon egg only, otherwise use default breed)
            if (block == BlockDragonBreedEgg.INSTANCE) {
                EnumDragonBreed breed = state.getValue(BlockDragonBreedEgg.BREED);
                dragon.getBreedHelper().setBreedType(breed);
            }
            
            world.spawnEntityInWorld(dragon);
        }
    }
}
