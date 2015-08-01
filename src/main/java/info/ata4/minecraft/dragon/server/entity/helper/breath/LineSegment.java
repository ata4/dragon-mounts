package info.ata4.minecraft.dragon.server.entity.helper.breath;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.*;

/**
 * Created by TGG on 31/07/2015.
 * LineSegment is used to provide ordered lists of line segments.
 *
 * Each line segment has a start point and a finish point.
 * The collection of LineSegments can be sorted in six different ways:
 * X_LOW = sort ascending by the smallest x coordinate
 * X_HIGH = sort ascending by the highest x coordinate
 * similar for Y_LOW, Y_HIGH, Z_LOW, Z_HIGH
 *
 * This is useful for culling line segments that don't overlap a given area
 * for example- you are trying to cull based on a location [x,y,z]
 * for a list sorted by X_LOW, when you find a segment with smallerPoint.xCoord > x, all higher LineSegments are culled
 * for a list sorted by X_HIGH, when you find a segment with largerPoint.xCoord <z, all lower LineSegments are culled
 *
 */
public class LineSegment
{
  public LineSegment(Vec3 point1, Vec3 point2)
  {
    smallerPoint = point1;
    largerPoint = point2;
  }

  /**
   * Make a deep copy of the LineSegment i.e. duplicate the points
   * @return the new deep copy
   */

  public LineSegment deepCopy()
  {
    return new LineSegment(smallerPoint, largerPoint);
  }

  public enum SortOrder {X_LOW, X_HIGH, Y_LOW, Y_HIGH, Z_LOW, Z_HIGH}

  public static void sort(List<LineSegment> lineSegments, SortOrder sortOrder)
  {
    switch (sortOrder) {
      case X_LOW:
      case X_HIGH: {
        for (LineSegment segment : lineSegments) {
          if (segment.smallerPoint.xCoord > segment.largerPoint.xCoord) {
            segment.swapPoints();
          }
        }
        Comparator<LineSegment> comparator = (sortOrder == SortOrder.X_LOW) ? new SortByXLow() : new SortByXHigh();
        lineSegments.sort(comparator);
        break;
      }
      case Y_LOW:
      case Y_HIGH: {
        for (LineSegment segment : lineSegments) {
          if (segment.smallerPoint.yCoord > segment.largerPoint.yCoord) {
            segment.swapPoints();
          }
        }
        Comparator<LineSegment> comparator = (sortOrder == SortOrder.Y_LOW) ? new SortByYLow() : new SortByYHigh();
        lineSegments.sort(comparator);
        break;
      }
      case Z_LOW:
      case Z_HIGH: {
        for (LineSegment segment : lineSegments) {
          if (segment.smallerPoint.zCoord > segment.largerPoint.zCoord) {
            segment.swapPoints();
          }
        }
        Comparator<LineSegment> comparator = (sortOrder == SortOrder.Z_LOW) ? new SortByZLow() : new SortByZHigh();
        lineSegments.sort(comparator);
        break;
      }
      default: {
        System.err.println("Illegal SortOrder in LineSegment.sort:" + sortOrder);
        return;
      }
    }
  }

  /**
   * Make a deep copy of the LineSegment List - i.e. will duplicate all the segments and all the Vec3
   * @param sourceList the LineSegment list to be copied, empty ok but not null!
   * @return the new deep copy
   */
  public static ArrayList<LineSegment> deepCopy(List<LineSegment> sourceList)
  {
    ArrayList<LineSegment> newCopy = new ArrayList<LineSegment>(sourceList.size());
    for (LineSegment lineSegment : sourceList) {
      newCopy.add(lineSegment.deepCopy());
    }
    return newCopy;
  }


  /**
   * Creates a cloud of points around the line segment, to simulate the movement of a sphere starting from the
   *   beginning of the line segment and moving to the end.  Each point is mapped onto the world grid.
   * Uses stochastic simulation, each point is generated as
   * 1) a point [x1,y1,z1] is chosen along the line segment, uniform probability distribution along the segment
   * 2) a random point [x2,y2,z2] is chosen within the sphere centred on [x1,y1,z1].  This is generated from spherical
   *    coordinates radius, phi, theta, uniformly distributed.  This puts more points near the centre of the sphere
   *    i.e. the density of points is highest in the centre which is roughly what we want.
   * Each call to addStochasticCloud adds a total of 1.00 to the world grid - eg 1.0 to a single location, or
   *   0.2 to location 1 and 0.8 to location 2, etc
   * @param hitDensity the density of points at each world grid location - is updated by the method
   * @param radius the radius of the sphere (blocks)
   */
  public void addStochasticCloud(Map<Vec3i, Float> hitDensity, double radius) {
    initialiseTables();
    final int NUMBER_OF_CLOUD_POINTS = 100;
    final float DENSITY_PER_POINT = 1.0F / 100;

    //    Equation of sphere converting from polar to cartesian:
    //    x = r.cos(theta).sin(phi)
    //    y = r.sin(theta).sin(phi)
    //    z = r.cos(phi)
    Random random = new Random();
    for (int i = 0; i < NUMBER_OF_CLOUD_POINTS; ++i) {
      double linePos = random.nextDouble();
      double x = MathX.lerp(smallerPoint.xCoord, largerPoint.xCoord, linePos);
      double y = MathX.lerp(smallerPoint.yCoord, largerPoint.yCoord, linePos);
      double z = MathX.lerp(smallerPoint.zCoord, largerPoint.zCoord, linePos);
      int theta = random.nextInt(TABLE_POINTS);
      int phi = random.nextInt(TABLE_POINTS);
      double r = random.nextDouble() * radius;
      double dx = r * cosTable[theta] * sinTable[phi];
      double dy = r * sinTable[theta] * sinTable[phi];
      double dz = r * cosTable[phi];
      Vec3i gridLoc = new Vec3i(x + dx, y + dy, z + dz);
      Float oldValue = hitDensity.get(gridLoc);
      if (oldValue == null) {
        oldValue = 0.0F;
      }
      hitDensity.put(gridLoc, oldValue + DENSITY_PER_POINT);
    }
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

  /**
   * Find the cubes that are overlapped by this segment and add them to the set
   * Is only approximate - uses a rectangular prism to approximate the beam
   * @param overlappedCubes the set of cubes ([x,y,z] blockpos), will be added to
   * @param radius approximate radius of beam in blocks;
   * @return
   */
  public void addOverlappedCubes(Set<Vec3i> overlappedCubes, double radius)
  {
    // uses a crude algorithm to find which blocks the segment overlaps:
    // chooses one of the three cardinal directions then takes slices along
    //  that direction at points [x1,y1,z1], [x2,y2,z2] etc
    //  on each slice, form a rectangle centred on the [x,y,z]
    //  all cubes partially overlapped by the rectangle are added to the set.
    // The rectangle height and width are expanded slightly depending on the
    //   slope of the segment - the cross-section through the prism gets bigger
    //   when it is cut at an angle
    // we always choose to iterate along the axis with greatest change relative
    //   to the other two

    double minX = Math.min(smallerPoint.xCoord, largerPoint.xCoord);
    double maxX = Math.max(smallerPoint.xCoord, largerPoint.xCoord);
    double minY = Math.min(smallerPoint.yCoord, largerPoint.yCoord);
    double maxY = Math.max(smallerPoint.yCoord, largerPoint.yCoord);
    double minZ = Math.min(smallerPoint.zCoord, largerPoint.zCoord);
    double maxZ = Math.max(smallerPoint.zCoord, largerPoint.zCoord);
    double deltaX = maxX - minX;
    double deltaY = maxY - minY;
    double deltaZ = maxZ - minZ;
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

    final double BLOCK_STEP_SIZE = 0.5;
    final int MAX_NUMBER_OF_STEPS = 10;
    double stepSize = Math.min(radius, BLOCK_STEP_SIZE);
    int numberOfSteps = (int)Math.ceil(length / stepSize);
    numberOfSteps = Math.min(numberOfSteps, MAX_NUMBER_OF_STEPS);

    int whichIsBiggest = 0;
    double biggestDelta = deltaX;
    if (deltaY > biggestDelta) {
      whichIsBiggest = 1;
      biggestDelta = deltaY;
    }
    if (deltaZ > biggestDelta) {
      whichIsBiggest = 2;
      biggestDelta = deltaZ;
    }
    final double ZERO_DELTA = 0.0001;
    if (biggestDelta < ZERO_DELTA) {
      whichIsBiggest = 3;
    }

    double halfSliceX = radius;
    double halfSliceY = radius;
    double halfSliceZ = radius;

    switch (whichIsBiggest) {
      case 0: {
        double halfSlice = radius * length / deltaX;
        double deltaYZ = Math.sqrt(deltaY*deltaY + deltaZ*deltaZ);
        halfSliceY = radius + (halfSlice - radius) * deltaY / deltaYZ;
        halfSliceZ = radius + (halfSlice - radius) * deltaZ / deltaYZ;
        break;
      }
      case 1: {
        double halfSlice = radius * length / deltaY;
        double deltaXZ = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        halfSliceX = radius + (halfSlice - radius) * deltaX / deltaXZ;
        halfSliceZ = radius + (halfSlice - radius) * deltaZ / deltaXZ;
        break;
      }
      case 2: {
        double halfSlice = radius * length / deltaZ;
        double deltaXY = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
        halfSliceX = radius + (halfSlice - radius) * deltaX / deltaXY;
        halfSliceY = radius + (halfSlice - radius) * deltaY / deltaXY;
        break;
      }
      case 3: {
        break;
      }
    }

    double x = minX;
    double y = minY;
    double z = minZ;
    for (int i = 0; i <= numberOfSteps; ++i) {
      switch (whichIsBiggest) {
        case 0: {
          addOverlappedAroundPointInXplane(overlappedCubes, x, y, z, halfSliceY, halfSliceZ);
          break;
        }
        case 1: {
          addOverlappedAroundPointInYplane(overlappedCubes, x, y, z, halfSliceX, halfSliceZ);
          break;
        }
        case 2: {
          addOverlappedAroundPointInZplane(overlappedCubes, x, y, z, halfSliceX, halfSliceY);
          break;
        }
        case 3: {  // no change at all -> point
          addOverlappedAroundPointInXplane(overlappedCubes, minX, minY, minZ, halfSliceY, halfSliceZ);
          addOverlappedAroundPointInYplane(overlappedCubes, minX, minY, minZ, halfSliceX, halfSliceZ);
          addOverlappedAroundPointInZplane(overlappedCubes, minX, minY, minZ, halfSliceX, halfSliceY);
          return;
        }
      }

      x += deltaX / numberOfSteps;
      y += deltaY / numberOfSteps;
      z += deltaZ / numberOfSteps;
    }
  }

  private void addOverlappedAroundPointInXplane(Set<Vec3i> overlappedPoints, double x, double y, double z,
                                               double halfSliceY, double halfSliceZ)
  {
    int minY = (int)(y - halfSliceY);
    int maxY = (int)(y + halfSliceY);
    int minZ = (int)(z - halfSliceZ);
    int maxZ = (int)(z + halfSliceZ);
    for (int iy = minY; iy <= maxY; ++iy) {
      for (int iz = minZ; iz <= maxZ; ++iz) {
         overlappedPoints.add(new Vec3i((int)x, iy, iz));
      }
    }
  }

  private void addOverlappedAroundPointInYplane(Set<Vec3i> overlappedPoints, double x, double y, double z,
                                               double halfSliceX, double halfSliceZ)
  {
    int minX = (int)(x - halfSliceX);
    int maxX = (int)(x + halfSliceX);
    int minZ = (int)(z - halfSliceZ);
    int maxZ = (int)(z + halfSliceZ);
    for (int ix = minX; ix <= maxX; ++ix) {
      for (int iz = minZ; iz <= maxZ; ++iz) {
        overlappedPoints.add(new Vec3i(ix, (int)y, iz));
      }
    }
  }

  private void addOverlappedAroundPointInZplane(Set<Vec3i> overlappedPoints, double x, double y, double z,
                                               double halfSliceX, double halfSliceY)
  {
    int minX = (int)(x - halfSliceX);
    int maxX = (int)(x + halfSliceX);
    int minY = (int)(y - halfSliceY);
    int maxY = (int)(y + halfSliceY);
    for (int ix = minX; ix <= maxX; ++ix) {
      for (int iy = minY; iy <= maxY; ++iy) {
        overlappedPoints.add(new Vec3i(ix, iy, (int)z));
      }
    }
  }

  private static class SortByXLow implements Comparator<LineSegment> {
    @Override
    public int compare(LineSegment o1, LineSegment o2) {
      return Double.compare(o1.smallerPoint.xCoord, o2.smallerPoint.xCoord);
    }
  }

  private static class SortByXHigh implements Comparator<LineSegment> {
    @Override
    public int compare(LineSegment o1, LineSegment o2) {
      return Double.compare(o1.largerPoint.xCoord, o2.largerPoint.xCoord);
    }
  }

  private static class SortByYLow implements Comparator<LineSegment> {
    @Override
    public int compare(LineSegment o1, LineSegment o2) {
      return Double.compare(o1.smallerPoint.yCoord, o2.smallerPoint.yCoord);
    }
  }

  private static class SortByYHigh implements Comparator<LineSegment> {
    @Override
    public int compare(LineSegment o1, LineSegment o2) {
      return Double.compare(o1.largerPoint.yCoord, o2.largerPoint.yCoord);
    }
  }

  private static class SortByZLow implements Comparator<LineSegment> {
    @Override
    public int compare(LineSegment o1, LineSegment o2) {
      return Double.compare(o1.smallerPoint.zCoord, o2.smallerPoint.zCoord);
    }
  }

  private static class SortByZHigh implements Comparator<LineSegment> {
    @Override
    public int compare(LineSegment o1, LineSegment o2) {
      return Double.compare(o1.largerPoint.zCoord, o2.largerPoint.zCoord);
    }
  }

  private void swapPoints() {
    Vec3 temp = smallerPoint;
    smallerPoint = largerPoint;
    largerPoint = temp;
  }

  public Vec3 smallerPoint;
    public Vec3 largerPoint;

}
