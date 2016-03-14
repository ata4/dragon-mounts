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

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Based on PathNavigateSwimmer but for air blocks.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PathNavigateFlying extends PathNavigate {

    public PathNavigateFlying(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected PathFinder getPathFinder() {
        return new PathFinder(new NodeProcessorFlying());
    }

    @Override
    protected Vec3 getEntityPosition() {
        return new Vec3(theEntity.posX, theEntity.posY + theEntity.height * 0.5, theEntity.posZ);
    }

    @Override
    protected boolean canNavigate() {
        return !isInLiquid();
    }

    @Override
    protected boolean isDirectPathBetweenPoints(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ) {
        MovingObjectPosition pos = worldObj.rayTraceBlocks(posVec31, getEntityPosition(), false, true, false);
        return pos == null || pos.typeOfHit == MovingObjectPosition.MovingObjectType.MISS;
    }
    
    @Override
    protected void pathFollow() {
        Vec3 pos = getEntityPosition();
        double entityWidthSq = theEntity.width * theEntity.width;

        if (pos.squareDistanceTo(currentPath.getVectorFromIndex(theEntity,
                currentPath.getCurrentPathIndex())) < entityWidthSq) {
            currentPath.incrementPathIndex();
        }

        int startPos = Math.min(currentPath.getCurrentPathIndex() + 6,
                currentPath.getCurrentPathLength() - 1);
        for (int i = startPos; i > currentPath.getCurrentPathIndex(); i--) {
            Vec3 posPoint = currentPath.getVectorFromIndex(theEntity, i);

            if (posPoint.squareDistanceTo(pos) <= 36 && isDirectPathBetweenPoints(pos, posPoint, 0, 0, 0)) {
                currentPath.setCurrentPathIndex(i);
                break;
            }
        }

        checkForStuck(pos);
    }
}
