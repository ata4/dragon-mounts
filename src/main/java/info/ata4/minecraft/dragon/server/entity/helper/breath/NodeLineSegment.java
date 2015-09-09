package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.Pair;
import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.*;

import java.util.*;

/**
 * Created by TGG on 31/07/2015.
 * NodeLineSegment is used to represent a spherical node which has moved from one [x,y,z] point to a second [x,y,z] point.
 * Each line segment has a start point and a finish point.  The node has a defined radius.
 * The line segment is then used to detect collisions with other objects
 *
 * Optionally, the segment
 *   can be provided with a collection of collisions as well (each collision corresponds to an AABB which is known to
 *   overlap with a block or entity, as discovered while moving the node.  the facing shows which face of the node
 *   collided with the object.)
 *  Typical usage:
 *  (1) create node segment with a start point, finish point, and node radius.  Optional collisions from entity moving.
 *  (2) collisionCheckAABB(), collisionCheckAABBcorners(), addStochasticCloud() and/or addBlockCollisions() to
 *      perform collision checks of the against the node against blocks or entities
 *
 */
public class NodeLineSegment
{
  public NodeLineSegment(Vec3 i_startPoint, Vec3 i_endPoint, float i_radius)
  {
    this(i_startPoint, i_endPoint, i_radius, null);
  }

  public NodeLineSegment(Vec3 i_startPoint, Vec3 i_endPoint, float i_radius,
                         Collection<Pair<EnumFacing, AxisAlignedBB>> i_collisions)
  {
    startPoint = i_startPoint;
    endPoint = i_endPoint;
    radius = i_radius;
    collisions = (i_collisions != null) ? i_collisions : new ArrayList<Pair<EnumFacing, AxisAlignedBB>>();
  }

  /**
   * Make a deep copy of the LineSegment i.e. duplicate the points
   * @return the new deep copy
   */

  public NodeLineSegment deepCopy()
  {
    return new NodeLineSegment(startPoint, endPoint, radius);
  }

  /**
   * Make a deep copy of the LineSegment List - i.e. will duplicate all the segments and all the Vec3
   * @param sourceList the LineSegment list to be copied, empty ok but not null!
   * @return the new deep copy
   */
  public static ArrayList<NodeLineSegment> deepCopy(List<NodeLineSegment> sourceList)
  {
    ArrayList<NodeLineSegment> newCopy = new ArrayList<NodeLineSegment>(sourceList.size());
    for (NodeLineSegment nodeLineSegment : sourceList) {
      newCopy.add(nodeLineSegment.deepCopy());
    }
    return newCopy;
  }

  public double getSegmentLength()
  {
    double deltaX = startPoint.xCoord - endPoint.xCoord;
    double deltaY = startPoint.yCoord - endPoint.yCoord;
    double deltaZ = startPoint.zCoord - endPoint.zCoord;
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    return length;
  }

  /** getChangeInValue the vector corresponding to the segment (from start point to end point)
   * @return
   */
  public Vec3 getSegmentDirection()
  {
    return new Vec3(endPoint.xCoord - startPoint.xCoord, endPoint.yCoord - startPoint.yCoord, endPoint.zCoord - startPoint.zCoord);
  }

  /** getChangeInValue an AABB which encompasses the entire line segment including the node radius around each end
   * @return
   */
  public AxisAlignedBB getAxisAlignedBoundingBox() {
    double minX = Math.min(startPoint.xCoord, endPoint.xCoord) - radius;
    double maxX = Math.max(startPoint.xCoord, endPoint.xCoord) + radius;
    double minY = Math.min(startPoint.yCoord, endPoint.yCoord) - radius;
    double maxY = Math.max(startPoint.yCoord, endPoint.yCoord) + radius;
    double minZ = Math.min(startPoint.zCoord, endPoint.zCoord) - radius;
    double maxZ = Math.max(startPoint.zCoord, endPoint.zCoord) + radius;
    AxisAlignedBB aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    for (Pair<EnumFacing, AxisAlignedBB> collision : collisions) {
      aabb = aabb.union(collision.getSecond());
    }
    return aabb;
  }

  /** return an AABB which contains all the line segments
   * @param nodeLineSegments
   * @return the AABB which contains all the line segments; null if collection empty
   */
  public static AxisAlignedBB getAxisAlignedBoundingBoxForAll(Collection<NodeLineSegment> nodeLineSegments)
  {
    if (nodeLineSegments == null || nodeLineSegments.isEmpty()) return null;

    AxisAlignedBB aabb = null;
    for (NodeLineSegment nodeLineSegment : nodeLineSegments) {
      aabb = (aabb == null) ? nodeLineSegment.getAxisAlignedBoundingBox()
                            : aabb.union(nodeLineSegment.getAxisAlignedBoundingBox());

    }
    return aabb;
  }

  /** Models collision between the node and the given aabb (of an entity)
   * Three checks:
   * a) For each of the direct collisions for this node (overlaps between the node AABB and the world, as calculated
   *   in the entity movement) - if any overlap occurs, apply the full density.  (Currently not used)
   * Otherwise:
   * b) stochastically, based on the area of effect on nearby objects, even if no direct AABB overlap.  see below.  This
   *   is most useful when the node is small compared to the aabb.
   * and
   * c) check corner of the aabb to see if it lies inside the nodelinesegment.  This is most useful when the node is large
   *    compared to the aabb.
   *
   * stochastically check how much the line segment collides with the specified aabb
   * Uses stochastic simulation, each point is generated as
   * 1) a point [x1,y1,z1] is chosen along the line segment, evenly distributed according to the number of cloud points,
   *    plus a small random jitter
   * 2) a random point [x2,y2,z2] is chosen within the sphere centred on [x1,y1,z1].  This is generated from spherical
   *    coordinates radius, phi, theta, uniformly distributed.  This puts more points near the centre of the sphere
   *    i.e. the density of points is highest in the centre which is roughly what we want.
   * @param aabb the aabb to check against
   * @param totalDensity the density of a complete collision
   * @param numberOfCloudPoints number of cloud points to use (1 - 1000) - clamped if out of range
   * @return a value from 0.0 (no collision) to totalDensity (total collision)
   */
  public float collisionCheckAABB(AxisAlignedBB aabb, float totalDensity, int numberOfCloudPoints)
  {
    for (Pair<EnumFacing, AxisAlignedBB> collision : collisions) {
      if (collision.getSecond().intersectsWith(aabb)) {
        return totalDensity;
      }
    }
    float stochasticHitDensity = collisionCheckAABBstochastic(aabb, totalDensity, numberOfCloudPoints);
    float aabbCornersHitDensity = collisionCheckAABBcorners(aabb, totalDensity);
    return Math.max(stochasticHitDensity, aabbCornersHitDensity);
  }

  /** Check all eight corners of the aabb to see how many lie within the nodelinesegment.
   * Most useful for when the aabb is smaller than the nodelinesegment
   * @param aabb the aabb to check against
   * @param totalDensity the density of a complete collision
   * @return a value from 0.0 (no collision) to totalDensity (total collision)
   */
  public float collisionCheckAABBcorners(AxisAlignedBB aabb, float totalDensity)
  {
    int cornersInside = 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.minX, aabb.minY, aabb.minZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.minX, aabb.minY, aabb.maxZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.minX, aabb.maxY, aabb.minZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.minX, aabb.maxY, aabb.maxZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.maxX, aabb.minY, aabb.minZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.maxX, aabb.minY, aabb.maxZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.maxX, aabb.maxY, aabb.minZ) ? 1 : 0;
    cornersInside += isPointWithinNodeLineSegment(aabb.maxX, aabb.maxY, aabb.maxZ) ? 1 : 0;
    return cornersInside * totalDensity / 8.0F;
  }

  /** check whether the given point lies within the nodeLineSegment (i.e. within the sphere around the start point,
   *   within the sphere around the end point, or within the cylinder about the line connecting start and end)
   * @param x [x,y,z] is the world coordinate to check
   * @return true if it lies inside, false otherwise
   */
  public boolean isPointWithinNodeLineSegment(double x, double y, double z)
  {
    // first, find the closest point on the line segment between start and finish.
    // This is given by the formula
    //  projection_of_u_on_v = v . ( u dot v) / length(v)^2
    // where u = vector from startpoint to test point, and v = vector from startpoint to endpoint

    Vec3 deltaAxis = endPoint.subtract(startPoint);
    Vec3 deltaPointToCheck = new Vec3(x - startPoint.xCoord, y - startPoint.yCoord, z - startPoint.zCoord);
    double deltaAxisLengthSq = deltaAxis.xCoord * deltaAxis.xCoord + deltaAxis.yCoord * deltaAxis.yCoord
                               + deltaAxis.zCoord * deltaAxis.zCoord;
    double dotProduct = deltaAxis.dotProduct(deltaPointToCheck);
    Vec3 closestPoint;
    if (dotProduct <= 0) {
      closestPoint = new Vec3(0,0,0);
    } else if (dotProduct >= deltaAxisLengthSq) {
      closestPoint = deltaAxis;
    } else {
      double projectionFraction = dotProduct / deltaAxisLengthSq;
      closestPoint = MathX.multiply(deltaAxis, projectionFraction);
    }
    return closestPoint.squareDistanceTo(deltaPointToCheck) <= radius * radius;
  }

  private final float NO_HIT_DENSITY_VALUE = 0.0F;

  /** Choose a number of random points from the nodelinesegment and see how many of them lie within the given aabb.
   * Most useful for when the aabb is larger than the nodelinesegment.
   * @param aabb the aabb to check against
   * @param totalDensity the density of a complete collision
   * @param numberOfCloudPoints number of cloud points to use (1 - 1000) - clamped if out of range
   * @return a value from 0.0 (no collision) to totalDensity (total collision)
   */
  private float collisionCheckAABBstochastic(AxisAlignedBB aabb, float totalDensity, int numberOfCloudPoints)
  {
    float retval = NO_HIT_DENSITY_VALUE;
    initialiseTables();
    final int MINIMUM_REASONABLE_CLOUD_POINTS = 1;
    final int MAXIMUM_REASONABLE_CLOUD_POINTS = 1000;
    numberOfCloudPoints = MathHelper.clamp_int(numberOfCloudPoints,
            MINIMUM_REASONABLE_CLOUD_POINTS, MAXIMUM_REASONABLE_CLOUD_POINTS);
    final int NUMBER_OF_CLOUD_POINTS = numberOfCloudPoints;
    final float DENSITY_PER_POINT = totalDensity / NUMBER_OF_CLOUD_POINTS;
    final double SUBSEGMENT_WIDTH = 1.0 / (NUMBER_OF_CLOUD_POINTS + 1);

    //    Equation of sphere converting from polar to cartesian:
    //    x = r.cos(theta).sin(phi)
    //    y = r.sin(theta).sin(phi)
    //    z = r.cos(phi)
    Random random = new Random();
    for (int i = 0; i < NUMBER_OF_CLOUD_POINTS; ++i) {
      double linePos = i * SUBSEGMENT_WIDTH;
      double jitter = random.nextDouble() * SUBSEGMENT_WIDTH;
      linePos += jitter;
      double x = MathX.lerp(startPoint.xCoord, endPoint.xCoord, linePos);
      double y = MathX.lerp(startPoint.yCoord, endPoint.yCoord, linePos);
      double z = MathX.lerp(startPoint.zCoord, endPoint.zCoord, linePos);
      int theta = random.nextInt(TABLE_POINTS);
      int phi = random.nextInt(TABLE_POINTS);
      double r = random.nextDouble() * radius;
      x +=  r * cosTable[theta] * sinTable[phi];
      y +=  r * sinTable[theta] * sinTable[phi];
      z +=  r * cosTable[phi];
      if (x >= aabb.minX && x <= aabb.maxX
              && y >= aabb.minY && y <= aabb.maxY
              && z >= aabb.minZ && z <= aabb.maxZ  ) {
        retval += DENSITY_PER_POINT;
      }
    }
    return retval;
  }


  /**
   * Creates a cloud of points around the line segment, to simulate the movement of a sphere starting from the
   *   beginning of the line segment and moving to the end.  Each point is mapped onto the world grid.
   * Uses stochastic simulation, each point is generated as
   * 1) a point [x1,y1,z1] is chosen along the line segment, evenly distributed according to the number of cloud points,
   *    plus a small random jitter
   * 2) a random point [x2,y2,z2] is chosen within the sphere centred on [x1,y1,z1].  This is generated from spherical
   *    coordinates radius, phi, theta, uniformly distributed.  This puts more points near the centre of the sphere
   *    i.e. the density of points is highest in the centre which is roughly what we want.
   * Each call to addStochasticCloud adds a total of totalDensity to the world grid -
   *   eg if totalDensity = 1.0, it adds 1.0 to a single location, or 0.2 to location 1 and 0.8 to location 2, etc
   * @param hitDensity the density of points at each world grid location - is updated by the method
   * @param totalDensity the total density to be added (eg 1.0F)
   * @param numberOfCloudPoints number of cloud points to use (1 - 1000) - clamped if out of range
   */
  public void addStochasticCloud(Map<Vec3i, BreathAffectedBlock> hitDensity, float totalDensity, int numberOfCloudPoints) {
    initialiseTables();
    final int MINIMUM_REASONABLE_CLOUD_POINTS = 1;
    final int MAXIMUM_REASONABLE_CLOUD_POINTS = 1000;
    numberOfCloudPoints = MathHelper.clamp_int(numberOfCloudPoints,
                                               MINIMUM_REASONABLE_CLOUD_POINTS, MAXIMUM_REASONABLE_CLOUD_POINTS);
    final int NUMBER_OF_CLOUD_POINTS = numberOfCloudPoints;
    final float DENSITY_PER_POINT = totalDensity / NUMBER_OF_CLOUD_POINTS;

    final double SUBSEGMENT_WIDTH = 1.0 / (NUMBER_OF_CLOUD_POINTS + 1);

    //    Equation of sphere converting from polar to cartesian:
    //    x = r.cos(theta).sin(phi)
    //    y = r.sin(theta).sin(phi)
    //    z = r.cos(phi)
    Random random = new Random();
    for (int i = 0; i < NUMBER_OF_CLOUD_POINTS; ++i) {
      double linePos = i * SUBSEGMENT_WIDTH;
      double jitter = random.nextDouble() * SUBSEGMENT_WIDTH;
      linePos += jitter;
      double x = MathX.lerp(startPoint.xCoord, endPoint.xCoord, linePos);
      double y = MathX.lerp(startPoint.yCoord, endPoint.yCoord, linePos);
      double z = MathX.lerp(startPoint.zCoord, endPoint.zCoord, linePos);
      int theta = random.nextInt(TABLE_POINTS);
      int phi = random.nextInt(TABLE_POINTS);
      double r = random.nextDouble() * radius;
      double dx = r * cosTable[theta] * sinTable[phi];
      double dy = r * sinTable[theta] * sinTable[phi];
      double dz = r * cosTable[phi];

      Vec3i gridLoc = new Vec3i(x + dx, y + dy, z + dz);

      BreathAffectedBlock breathAffectedBlock = hitDensity.get(gridLoc);
      if (breathAffectedBlock == null) {
        breathAffectedBlock = new BreathAffectedBlock();
      }
      EnumFacing faceHit = getIntersectedFace(x, y, z, x+dx, y+dy, z+dz);
      breathAffectedBlock.addHitDensity(faceHit, DENSITY_PER_POINT);
      hitDensity.put(gridLoc, breathAffectedBlock);
    }
  }

  /**
   * For each of the direct collisions for this node (overlaps between the node AABB and the world, as calculated
   *   in the entity movement), increment the hit density of the corresponding blocks
   * (The collision may have been caused by an entity not the blocks, however if the block actually has nothing in
   *   it then it won't be affected anyway.)
   * @param hitDensity the density of points at each world grid location - is updated by the method
   * @param densityPerCollision the total density to be added (eg 1.0F)
   */
  public void addBlockCollisions(Map<Vec3i, BreathAffectedBlock> hitDensity, float densityPerCollision) {

    for (Pair<EnumFacing, AxisAlignedBB> collision : collisions) {
      final double CONTRACTION = 0.001;
      AxisAlignedBB aabb = collision.getSecond();
      if (aabb.maxX - aabb.minX > 2 * CONTRACTION
          && aabb.maxY - aabb.minY > 2 * CONTRACTION
          && aabb.maxZ - aabb.minZ > 2 * CONTRACTION) {
        aabb = aabb.contract(CONTRACTION, CONTRACTION, CONTRACTION);
        BlockPos blockposMin = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
        BlockPos blockposMax = new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ);

        Iterator<BlockPos.MutableBlockPos> iterator = BlockPos.getAllInBox(blockposMin, blockposMax).iterator();
        while (iterator.hasNext()) {
          BlockPos blockpos = iterator.next();
          BreathAffectedBlock breathAffectedBlock = hitDensity.get(blockpos);
          if (breathAffectedBlock == null) {
            breathAffectedBlock = new BreathAffectedBlock();
          }
          breathAffectedBlock.addHitDensity(collision.getFirst().getOpposite(), densityPerCollision);
          hitDensity.put(blockpos, breathAffectedBlock);
        }
      }
    }
  }

  /**
   * Given a ray which originated at xyzOrigin and terminated at xyzHit:
   * Find which face of the block at xyzHit was hit by the ray.
   * @param xOrigin
   * @param yOrigin
   * @param zOrigin
   * @param xHit
   * @param yHit
   * @param zHit
   * @return the face which was hit.  If none (was inside block), returns null
   */
  public static EnumFacing getIntersectedFace(double xOrigin, double yOrigin, double zOrigin,
                                              double xHit, double yHit, double zHit)
  {
    AxisAlignedBB aabb = new AxisAlignedBB(Math.floor(xHit), Math.floor(yHit), Math.floor(zHit),
            Math.ceil(xHit), Math.ceil(yHit), Math.ceil(zHit));

    MovingObjectPosition mop = aabb.calculateIntercept(new Vec3(xOrigin, yOrigin, zOrigin), new Vec3(xHit, yHit, zHit));
    if (mop == null) return null;
    return mop.sideHit;
  }


  private static boolean tablesInitialised = false;
  private static final int TABLE_POINTS = 256;
  private static double [] sinTable = new double[TABLE_POINTS];
  private static double [] cosTable = new double[TABLE_POINTS];

  private static void initialiseTables()
  {
    if (tablesInitialised) return;
    for (int i = 0; i < TABLE_POINTS; ++i) {
      double angle = i * 2.0 * Math.PI / TABLE_POINTS;
      sinTable[i] = Math.sin(angle);
      cosTable[i] = Math.cos(angle);
    }
    tablesInitialised = true;
  }


  public Vec3 startPoint;
  public Vec3 endPoint;
  public float radius;
  private Collection<Pair<EnumFacing, AxisAlignedBB>> collisions;
}
