import info.ata4.minecraft.dragon.server.entity.helper.breath.LineSegment;
import junit.framework.AssertionFailedError;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Created by TGG on 31/07/2015.
 */
public class LineSegmentTest
{
  @Test
  public void testSort() throws Exception
  {
    final int TEST_COUNT = 100;
    Random random = new Random();
    ArrayList<LineSegment> array1 = generateRandom(random, TEST_COUNT);
    LineSegment.sort(array1, LineSegment.SortOrder.X_LOW);
    double lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array1) {
      assertTrue(segment.smallerPoint.xCoord >= lastValue);
      assertTrue(segment.smallerPoint.xCoord <= segment.largerPoint.xCoord);
      lastValue = segment.smallerPoint.xCoord;
    }

    ArrayList<LineSegment> array2 = generateRandom(random, TEST_COUNT);
    LineSegment.sort(array2, LineSegment.SortOrder.X_HIGH);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array2) {
      assertTrue(segment.largerPoint.xCoord >= lastValue);
      assertTrue(segment.smallerPoint.xCoord <= segment.largerPoint.xCoord);
      lastValue = segment.largerPoint.xCoord;
    }

    ArrayList<LineSegment> array3 = generateRandom(random, TEST_COUNT);
    LineSegment.sort(array3, LineSegment.SortOrder.Y_LOW);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array3) {
      assertTrue(segment.smallerPoint.yCoord >= lastValue);
      assertTrue(segment.smallerPoint.yCoord <= segment.largerPoint.yCoord);
      lastValue = segment.smallerPoint.yCoord;
    }

    ArrayList<LineSegment> array4 = generateRandom(random, TEST_COUNT);
    LineSegment.sort(array4, LineSegment.SortOrder.Y_HIGH);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array4) {
      assertTrue(segment.largerPoint.yCoord >= lastValue);
      assertTrue(segment.smallerPoint.yCoord <= segment.largerPoint.yCoord);
      lastValue = segment.largerPoint.yCoord;
    }

    ArrayList<LineSegment> array5 = generateRandom(random, TEST_COUNT);
    LineSegment.sort(array5, LineSegment.SortOrder.Z_LOW);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array5) {
      assertTrue(segment.smallerPoint.zCoord >= lastValue);
      assertTrue(segment.smallerPoint.zCoord <= segment.largerPoint.zCoord);
      lastValue = segment.smallerPoint.zCoord;
    }

    ArrayList<LineSegment> array6 = generateRandom(random, TEST_COUNT);
    LineSegment.sort(array6, LineSegment.SortOrder.Z_HIGH);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array6) {
      assertTrue(segment.largerPoint.zCoord >= lastValue);
      assertTrue(segment.smallerPoint.zCoord <= segment.largerPoint.zCoord);
      lastValue = segment.largerPoint.zCoord;
    }


    // sort a second time; no effect
    LineSegment.sort(array1, LineSegment.SortOrder.X_LOW);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array1) {
      assertTrue(segment.smallerPoint.xCoord >= lastValue);
      assertTrue(segment.smallerPoint.xCoord <= segment.largerPoint.xCoord);
      lastValue = segment.smallerPoint.xCoord;
    }

    // resort in a different order
    LineSegment.sort(array1, LineSegment.SortOrder.Z_HIGH);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array1) {
      assertTrue(segment.largerPoint.zCoord >= lastValue);
      assertTrue(segment.smallerPoint.zCoord <= segment.largerPoint.zCoord);
      lastValue = segment.largerPoint.zCoord;
    }

    // test deepCopy

          // deepcopy sort matches original array1
    ArrayList<LineSegment> deepCopy = LineSegment.deepCopy(array1);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : deepCopy) {
      assertTrue(segment.largerPoint.zCoord >= lastValue);
      assertTrue(segment.smallerPoint.zCoord <= segment.largerPoint.zCoord);
      lastValue = segment.largerPoint.zCoord;
    }

    // sort deepcopy and check correctly sorted
    LineSegment.sort(deepCopy, LineSegment.SortOrder.Y_LOW);
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : deepCopy) {
      assertTrue(segment.smallerPoint.yCoord >= lastValue);
      assertTrue(segment.smallerPoint.yCoord <= segment.largerPoint.yCoord);
      lastValue = segment.smallerPoint.yCoord;
    }

        // original array still sorted properly as Z_HIGH
    lastValue = Double.MIN_VALUE;
    for (LineSegment segment : array1) {
      assertTrue(segment.largerPoint.zCoord >= lastValue);
      assertTrue(segment.smallerPoint.zCoord <= segment.largerPoint.zCoord);
      lastValue = segment.largerPoint.zCoord;
    }


  }

  private ArrayList<LineSegment> generateRandom(Random random, int count)
  {
    ArrayList<LineSegment> newArray = new ArrayList<LineSegment>(count);
    for (int i = 0; i < count; ++i) {
      newArray.add(generateRandomSegment(random));
    }
    return newArray;
  }

  private LineSegment generateRandomSegment(Random random)
  {
    Vec3 point1 = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
    Vec3 point2 = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
    return new LineSegment(point1, point2);
  }

  @Test
  public void testAddOverlappedCubes() throws Exception
  {
    HashSet<Vec3i> overlappedCubes = new HashSet<Vec3i>();
    Vec3 point1 = new Vec3(0.5, 0.5, 0.5);
    Vec3 point2 = new Vec3(4.5, 0.5, 0.5);
    LineSegment testSegment = new LineSegment(point1, point2);
    testSegment.addOverlappedCubes(overlappedCubes, 0.1);

    // expect: [0,0,0], [1,0,0] ... [4,0,0]
    List<Vec3i> allOverlaps = new ArrayList<Vec3i>(overlappedCubes);
    assertTrue(allOverlaps.size() == 5);

    HashSet<Vec3i> expected = new HashSet<Vec3i>();
    expected.add(new Vec3i(0, 0, 0));
    expected.add(new Vec3i(1, 0, 0));
    expected.add(new Vec3i(2, 0, 0));
    expected.add(new Vec3i(3, 0, 0));
    expected.add(new Vec3i(4, 0, 0));

    for (Vec3i entry : allOverlaps) {
      assertTrue(expected.contains(entry));
      expected.remove(entry);
    }
  }

  @Test
  public void testAddStochasticCloud() throws Exception
  {
    HashMap<Vec3i, Float> densityMap = new HashMap<Vec3i, Float>();
    Vec3 point1 = new Vec3(0.5, 0.5, 0.5);
    Vec3 point2 = new Vec3(4.5, 0.5, 0.5);
    LineSegment testSegment = new LineSegment(point1, point2);
    final float TOTAL_DENSITY_1 = 1000.0F;
    testSegment.addStochasticCloud(densityMap, 0.45, TOTAL_DENSITY_1, 100);

    // expect: [0,0,0], [1,0,0] ... [4,0,0] only
    // also expect total density approx = TOTAL_DENSITY within roundoff
    List<Vec3i> allOverlaps = new ArrayList<Vec3i>(densityMap.keySet());
    assertTrue(allOverlaps.size() == 5);

    HashSet<Vec3i> expected = new HashSet<Vec3i>();
    expected.add(new Vec3i(0, 0, 0));
    expected.add(new Vec3i(1, 0, 0));
    expected.add(new Vec3i(2, 0, 0));
    expected.add(new Vec3i(3, 0, 0));
    expected.add(new Vec3i(4, 0, 0));

    Float totalDensity = 0F;
    for (Vec3i entry : allOverlaps) {
      totalDensity += densityMap.get(entry);
      assertTrue(expected.contains(entry));
      expected.remove(entry);
    }
    assertTrue(Math.abs(totalDensity - TOTAL_DENSITY_1) < TOTAL_DENSITY_1 / 10000.0F);

  }

}