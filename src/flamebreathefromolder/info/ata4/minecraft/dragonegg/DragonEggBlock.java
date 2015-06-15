/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragonegg;

import info.ata4.minecraft.GameUtils;
import info.ata4.minecraft.dragon.RidableVolantDragon;
import java.util.Random;
import net.minecraft.src.*;

/**
 * A static dragon egg block.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonEggBlock extends BlockDragonEgg {

    public DragonEggBlock(int x, int y) {
        super(x, y);
    }
    
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        for (int l = 0; l < 2; l++) {
            double px = x + 0.5 + (rand.nextDouble() - 0.5);
            double py = y + 0.5 + (rand.nextDouble() - 0.5);
            double pz = z + 0.5 + (rand.nextDouble() - 0.5);
            double ox = (rand.nextDouble() - 0.5) * 2;
            double oy = (rand.nextDouble() - 0.5) * 2;
            double oz = (rand.nextDouble() - 0.5) * 2;
            world.spawnParticle("portal", px, py, pz, ox, oy, oz);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        tryToFall(world, x, y, z);
    }

    private void tryToFall(World world, int x, int y, int z) {
        int xOld = x;
        int yOld = y;
        int zOld = z;
        
        if (BlockSand.canFallBelow(world, xOld, yOld - 1, zOld) && yOld >= 0) {
            byte chunkArea = 32;
            if (!world.checkChunksExist(x - chunkArea, y - chunkArea, z - chunkArea, x + chunkArea, y + chunkArea, z + chunkArea)) {
                world.setBlockWithNotify(x, y, z, 0);
                for (; BlockSand.canFallBelow(world, x, y - 1, z) && y > 0; y--) {
                }
                if (y > 0) {
                    world.setBlockWithNotify(x, y, z, blockID);
                }
            } else {
                DragonEgg egg = new DragonEgg(world, (float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
                egg.fallTime = 0;
                world.spawnEntityInWorld(egg);
            }
        }
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.multiplayerWorld) {
            world.setBlockWithNotify(x, y, z, 0);
            
            boolean launch = GameUtils.consumePlayerEquippedItem(player, Item.saddle);

            if (launch) {
                DragonEgg egg = new DragonEgg(world, (float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
                world.spawnEntityInWorld(egg);
                player.mountEntity(egg);
                egg.launch();
                
                // boom!
                world.createExplosion(player, x, y - 1, z, 0);
            } else {
                RidableVolantDragon dragon = new RidableVolantDragon(world);
                dragon.setPosition(x, y, z);
                dragon.onGround = true;
                
                // spawn only if there's enough room
                if (dragon.getCanSpawnHere()) {
                    world.spawnEntityInWorld(dragon);
                    dragon.appear();
                } else {
                    world.setBlockWithNotify(x, y, z, blockID);
                    player.addChatMessage("mod.dragonmount.noroom");
                }
            }
        }
        
        return true;
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
    }
}
