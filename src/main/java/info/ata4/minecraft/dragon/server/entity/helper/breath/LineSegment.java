package info.ata4.minecraft.dragon.server.entity.helper.breath;

import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
