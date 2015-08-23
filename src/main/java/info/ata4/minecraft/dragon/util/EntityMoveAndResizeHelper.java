package info.ata4.minecraft.dragon.util;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by TGG on 16/08/2015.
 * Utility class to resize an entity
 * 1) resizes the entity around its centre
 * 2) takes into account any nearby objects that the entity might collide with
 */
public class EntityMoveAndResizeHelper {

  public EntityMoveAndResizeHelper(Entity parentEntity)
  {
    entity = parentEntity;
  }
  /**
   * Tries to moves the entity by the passed in displacement. Args: dx, dy, dz
   * Copied from vanilla; irrelevant parts deleted; modify to accommodate a change in size
   * expands the entity around the centre position:
   *   if the expansion causes it to bump against another collision box, temporarily ignore the expansion on
   *   that side.  bumping into x also constrains z because width is common to both.
   * @param dx dx, dy, dz are the desired movement/displacement of the entity
   * @param newHeight the new entity height
   * @param newWidth the new entity width
   *@return returns a collection showing which parts of the entity collided with an object- eg
   *        (WEST, [3,2,6]-->[3.5, 2, 6] means the west face of the entity collided; the entity tried to move to
   *          x = 3, but got pushed back to x=3.5
   */

  public Collection<Pair<EnumFacing, AxisAlignedBB>> moveAndResizeEntity(double dx, double dy, double dz, float newWidth, float newHeight) {
    entity.worldObj.theProfiler.startSection("moveflame");
    AxisAlignedBB entityAABB = entity.getEntityBoundingBox().offset(0, 0, 0);  // get a copy

    double wDXplus = (newWidth - entity.width) / 2.0;
    double wDYplus = (newHeight - entity.height) / 2.0;
    double wDZplus = (newWidth - entity.width) / 2.0;
    double wDXneg = -wDXplus;
    double wDYneg = -wDYplus;
    double wDZneg = -wDZplus;

    AxisAlignedBB collisionZone = entityAABB.addCoord(wDXneg, wDYneg, wDZneg)
                                            .addCoord(wDXplus, wDYplus, wDZplus)
                                            .addCoord(dx, dy, dz);
    List<AxisAlignedBB> collidingAABB = entity.worldObj.getCollidingBoundingBoxes(entity, collisionZone);

    if (MathX.isSignificantlyDifferent(newHeight, entity.height)) {
      for (AxisAlignedBB aabb : collidingAABB) {
        wDYplus = aabb.calculateYOffset(entityAABB, wDYplus);
        wDYneg = aabb.calculateYOffset(entityAABB, wDYneg);
      }
      entity.height += (wDYplus - wDYneg);
    } else {
      wDYplus = 0;
      wDYneg = 0;
    }

    if (MathX.isSignificantlyDifferent(newWidth, entity.width)) {
      for (AxisAlignedBB aabb : collidingAABB) {
        wDXplus = aabb.calculateXOffset(entityAABB, wDXplus);
        wDXneg = aabb.calculateXOffset(entityAABB, wDXneg);
        wDZplus = aabb.calculateZOffset(entityAABB, wDZplus);
        wDZneg = aabb.calculateZOffset(entityAABB, wDZneg);
      }
      // constrain width based on both x and z collisions to make sure width remains equal for x and z
      wDXplus = Math.min(wDXplus, wDZplus);
      wDXneg = Math.max(wDXneg, wDZneg);
      wDZplus = wDXplus;
      wDZneg = wDXneg;
      entity.width += (wDXplus - wDXneg);
    } else {
      wDXplus = 0;
      wDXneg = 0;
      wDZplus = 0;
      wDZneg = 0;
    }

    entityAABB = new AxisAlignedBB(entityAABB.minX + wDXneg, entityAABB.minY + wDYneg, entityAABB.minZ + wDZneg,
                                   entityAABB.maxX + wDXplus, entityAABB.maxY + wDYplus, entityAABB.maxZ + wDZplus);

    double desiredDX = dx;
    double desiredDY = dy;
    double desiredDZ = dz;

    for (AxisAlignedBB aabb : collidingAABB) {
      dy = aabb.calculateYOffset(entityAABB, dy);
    }
    entityAABB = entityAABB.offset(0, dy, 0);

    for (AxisAlignedBB aabb : collidingAABB) {
      dx = aabb.calculateXOffset(entityAABB, dx);
    }
    entityAABB = entityAABB.offset(dx, 0, 0);

    for (AxisAlignedBB aabb : collidingAABB) {
      dz = aabb.calculateZOffset(entityAABB, dz);
    }
    entityAABB = entityAABB.offset(0, 0, dz);
    entity.setEntityBoundingBox(entityAABB);

    entity.posX = (entityAABB.minX + entityAABB.maxX) / 2.0;
    entity.posY = entityAABB.minY;
    entity.posZ = (entityAABB.minZ + entityAABB.maxZ) / 2.0;

    entity.isCollidedHorizontally = desiredDX != dx || desiredDZ != dz;
    entity.isCollidedVertically = desiredDY != dy;
    entity.onGround = entity.isCollidedVertically && desiredDY < 0.0;
    entity.isCollided = entity.isCollidedHorizontally || entity.isCollidedVertically;

    // if we collided in any direction, stop the entity's motion in that direction, and mark the truncated zone
    //   as a collision zone - i.e if we wanted to move to dx += 0.5, but actually could only move +0.2, then the
    //   collision zone is the region from +0.2 to +0.5
    collisions.clear();
    if (desiredDX != dx) {
      entity.motionX = 0.0D;
      AxisAlignedBB collidedZone;
      if (desiredDX < 0) {
        collidedZone = new AxisAlignedBB(entityAABB.minX + (desiredDX - dx), entityAABB.minY, entityAABB.minZ,
                                         entityAABB.minX, entityAABB.maxY, entityAABB.maxZ);
        collisions.add(new Pair<EnumFacing, AxisAlignedBB>(EnumFacing.WEST, collidedZone));
      } else {
        collidedZone = new AxisAlignedBB(entityAABB.maxX, entityAABB.minY, entityAABB.minZ,
                                         entityAABB.maxX + (desiredDX - dx), entityAABB.maxY, entityAABB.maxZ);
        collisions.add(new Pair<EnumFacing, AxisAlignedBB>(EnumFacing.EAST, collidedZone));
      }
    }

    if (desiredDY != dy) {
      entity.motionY = 0.0D;
      AxisAlignedBB collidedZone;
      if (desiredDY < 0) {
        collidedZone = new AxisAlignedBB(entityAABB.minX,  entityAABB.minY + (desiredDY - dy), entityAABB.minZ,
                                         entityAABB.maxX, entityAABB.minY, entityAABB.maxZ);
        collisions.add(new Pair<EnumFacing, AxisAlignedBB>(EnumFacing.DOWN, collidedZone));
      } else {
        collidedZone = new AxisAlignedBB(entityAABB.minX, entityAABB.maxY, entityAABB.minZ,
                                         entityAABB.maxX, entityAABB.maxY + (desiredDY - dy), entityAABB.maxZ);
        collisions.add(new Pair<EnumFacing, AxisAlignedBB>(EnumFacing.UP, collidedZone));
      }
    }

    if (desiredDZ != dz) {
      entity.motionZ = 0.0D;
      AxisAlignedBB collidedZone;
      if (desiredDZ < 0) {
        collidedZone = new AxisAlignedBB(entityAABB.minX, entityAABB.minY, entityAABB.minZ + (desiredDZ - dz),
                                         entityAABB.maxX, entityAABB.maxY, entityAABB.minZ);
        collisions.add(new Pair<EnumFacing, AxisAlignedBB>(EnumFacing.NORTH, collidedZone));
      } else {
        collidedZone = new AxisAlignedBB(entityAABB.minX, entityAABB.minY, entityAABB.maxZ,
                                         entityAABB.maxX, entityAABB.maxY, entityAABB.maxZ + (desiredDZ - dz));
        collisions.add(new Pair<EnumFacing, AxisAlignedBB>(EnumFacing.SOUTH, collidedZone));
      }
    }
    entity.worldObj.theProfiler.endSection();
    return collisions;
  }

  // a record of which parts of the entity collided with an object during moving
  // each entry is the face of the entity and the zone (AABB) that collided
  // eg (WEST, [3,2,6]-->[3.5, 2, 6] means the west face of the entity collided; the entity tried to move to
  //   x = 3, but got pushed back out to x=3.5
  private List<Pair<EnumFacing, AxisAlignedBB>> collisions = new ArrayList<Pair<EnumFacing, AxisAlignedBB>>();

  private Entity entity;
}
