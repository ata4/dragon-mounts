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

/**
 * Based on SwimNodeProcessor but for air blocks.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class NodeProcessorFlying extends NodeProcessor {

    @Override
    public void initProcessor(IBlockAccess iblockaccessIn, Entity entityIn) {
        super.initProcessor(iblockaccessIn, entityIn);
    }

    /**
     * This method is called when all nodes have been processed and PathEntity
     * is created.
     * {@link net.minecraft.world.pathfinder.WalkNodeProcessor WalkNodeProcessor}
     * uses this to change its field {@link
     * net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater avoidsWater}
     */
    @Override
    public void postProcess() {
        super.postProcess();
    }

    /**
     * Returns given entity's position as PathPoint
     */
    @Override
    public PathPoint getPathPointTo(Entity entityIn) {
        return openPoint(
            MathHelper.floor_double(entityIn.getEntityBoundingBox().minX),
            MathHelper.floor_double(entityIn.getEntityBoundingBox().minY + 0.5D),
            MathHelper.floor_double(entityIn.getEntityBoundingBox().minZ)
        );
    }

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
        int i = findSafePoint(entityIn, x, y, z);
        return i == -1 ? openPoint(x, y, z) : null;
    }

    private int findSafePoint(Entity entityIn, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int ix = x; ix < x + entitySizeX; ++ix) {
            for (int iy = y; iy < y + entitySizeY; ++iy) {
                for (int iz = z; iz < z + entitySizeZ; ++iz) {
                    Block block = blockaccess.getBlockState(pos.set(ix, iy, iz)).getBlock();

                    if (block.getMaterial() != Material.air) {
                        return 0;
                    }
                }
            }
        }

        return -1;
    }
}
