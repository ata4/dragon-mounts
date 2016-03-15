/*
** 2016 March 13
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.path;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.pathfinder.NodeProcessor;
import net.minecraft.world.pathfinder.SwimNodeProcessor;

/**
 * Based on SwimNodeProcessor but for air blocks.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class NodeProcessorFlying extends SwimNodeProcessor {

    /**
     * Returns PathPoint for given coordinates
     */
    @Override
    public PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double target) {
        return openPoint(
            MathHelper.floor_double(x - (entityIn.width / 2.0)),
            MathHelper.floor_double(y + 0.5),
            MathHelper.floor_double(target - (entityIn.width / 2.0))
        );
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        int i = 0;
        
        for (EnumFacing facing : EnumFacing.values()) {
            PathPoint point = getSafePoint(entityIn,
                currentPoint.xCoord + facing.getFrontOffsetX(),
                currentPoint.yCoord + facing.getFrontOffsetY(),
                currentPoint.zCoord + facing.getFrontOffsetZ()
            );

            if (point != null && !point.visited && point.distanceTo(targetPoint) < maxDistance) {
                pathOptions[i++] = point;
            }
        }

        return i;
    }

    /**
     * Returns a point that the entity can safely move to
     */
    private PathPoint getSafePoint(Entity entityIn, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        
        entitySizeX = MathHelper.floor_float(entityIn.width + 1);
        entitySizeY = MathHelper.floor_float(entityIn.height + 1);
        entitySizeZ = MathHelper.floor_float(entityIn.width + 1);

        for (int ix = 0; ix < entitySizeX; ++ix) {
            for (int iy = 0; iy < entitySizeY; ++iy) {
                for (int iz = 0; iz < entitySizeZ; ++iz) {
                    Block block = blockaccess.getBlockState(pos.add(ix, iy, iz)).getBlock();
                    if (block.getMaterial() != Material.air) {
                        return null;
                    }
                }
            }
        }

        return openPoint(x, y, z);
    }
}
