package info.ata4.minecraft.dragon.server.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TGG on 8/07/2015.
 * Performs a ray trace of the player's line of sight to see what the player is looking at.
 * Similar to the vanilla getMouseOver, which is client side only.
 */
public class RayTraceServer
{
  /**
   * Find what the player is looking at (block or entity), up to a maximum range
   * based on code from EntityRenderer.getMouseOver
   * Will not target entities which are tamed by the player
   * @return the block or entity that the player is looking at / targeting with their cursor.  null if no collision
   */
  public static MovingObjectPosition getMouseOver(World world, EntityPlayer entityPlayerSP, float maxDistance) {
    final float PARTIAL_TICK = 1.0F;
    Vec3 positionEyes = entityPlayerSP.getPositionEyes(PARTIAL_TICK);
    Vec3 lookDirection = entityPlayerSP.getLook(PARTIAL_TICK);
    Vec3 endOfLook = positionEyes.addVector(lookDirection.xCoord * maxDistance,
            lookDirection.yCoord * maxDistance,
            lookDirection.zCoord * maxDistance);
    final boolean STOP_ON_LIQUID = true;
    final boolean IGNORE_BOUNDING_BOX = true;
    final boolean RETURN_NULL_IF_NO_COLLIDE = true;
    MovingObjectPosition targetedBlock = world.rayTraceBlocks(positionEyes, endOfLook,
            STOP_ON_LIQUID, !IGNORE_BOUNDING_BOX,
            !RETURN_NULL_IF_NO_COLLIDE);

    double collisionDistanceSQ = maxDistance * maxDistance;
    if (targetedBlock != null) {
      collisionDistanceSQ = targetedBlock.hitVec.squareDistanceTo(positionEyes);
      endOfLook = targetedBlock.hitVec;
    }

    final float EXPAND_SEARCH_BOX_BY = 1.0F;
    AxisAlignedBB searchBox = entityPlayerSP.getEntityBoundingBox();
    Vec3 endOfLookDelta = endOfLook.subtract(positionEyes);
    searchBox = searchBox.addCoord(endOfLookDelta.xCoord, endOfLookDelta.yCoord, endOfLookDelta.zCoord);
    searchBox = searchBox.expand(EXPAND_SEARCH_BOX_BY, EXPAND_SEARCH_BOX_BY, EXPAND_SEARCH_BOX_BY);
    List<Entity> nearbyEntities = (List<Entity>) world.getEntitiesWithinAABBExcludingEntity(
            entityPlayerSP, searchBox);
    Entity closestEntityHit = null;
    double closestEntityDistanceSQ = Double.MAX_VALUE;
    for (Entity entity : nearbyEntities) {
      if (!entity.canBeCollidedWith() || entity == entityPlayerSP.ridingEntity) {
        continue;
      }
      if (entity instanceof EntityTameable) {
        EntityTameable tamedEntity = (EntityTameable)entity;
        if (tamedEntity.isOwner(entityPlayerSP)) {
          continue;
        }
      }

      float collisionBorderSize = entity.getCollisionBorderSize();
      AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
              .expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
      MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(positionEyes, endOfLook);

      if (axisalignedbb.isVecInside(endOfLook)) {
        double distanceSQ = (movingobjectposition == null) ? positionEyes.squareDistanceTo(endOfLook)
                : positionEyes.squareDistanceTo(movingobjectposition.hitVec);
        if (distanceSQ <= closestEntityDistanceSQ) {
          closestEntityDistanceSQ = distanceSQ;
          closestEntityHit = entity;
        }
      } else if (movingobjectposition != null) {
        double distanceSQ = positionEyes.squareDistanceTo(movingobjectposition.hitVec);
        if (distanceSQ <= closestEntityDistanceSQ) {
          closestEntityDistanceSQ = distanceSQ;
          closestEntityHit = entity;
        }
      }
    }

    if (closestEntityDistanceSQ <= collisionDistanceSQ) {
      assert (closestEntityHit != null);
      return new MovingObjectPosition(closestEntityHit, closestEntityHit.getPositionVector());
    }
    return targetedBlock;
  }
}
