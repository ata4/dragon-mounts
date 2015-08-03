import info.ata4.minecraft.dragon.server.entity.helper.breath.NodeLineSegment;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Created by TGG on 31/07/2015.
 */
public class NodeLineSegmentTest
{

  private ArrayList<NodeLineSegment> generateRandom(Random random, int count, float radius)
  {
    ArrayList<NodeLineSegment> newArray = new ArrayList<NodeLineSegment>(count);
    for (int i = 0; i < count; ++i) {
      newArray.add(generateRandomSegment(random, radius));
    }
    return newArray;
  }

  private NodeLineSegment generateRandomSegment(Random random, float radius)
  {
    Vec3 point1 = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
    Vec3 point2 = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
    return new NodeLineSegment(point1, point2, radius);
  }

  @Test
  public void testAddStochasticCloud() throws Exception
  {
    final float RADIUS = 0.45F;
    HashMap<Vec3i, Float> densityMap = new HashMap<Vec3i, Float>();
    Vec3 point1 = new Vec3(0.5, 0.5, 0.5);
    Vec3 point2 = new Vec3(4.5, 0.5, 0.5);
    NodeLineSegment testSegment = new NodeLineSegment(point1, point2, RADIUS);
    final float TOTAL_DENSITY_1000 = 1000.0F;
    testSegment.addStochasticCloud(densityMap, TOTAL_DENSITY_1000, 100);

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
    assertTrue(Math.abs(totalDensity - TOTAL_DENSITY_1000) < TOTAL_DENSITY_1000 / 10000.0F);
  }

  @Test
  public void testCollisionCheckAABB() throws Exception
  {
    final float RADIUS = 0.45F;
    Vec3 point1 = new Vec3(0.5, 1.0, 2.0);
    Vec3 point2 = new Vec3(4.5, 5.5, 8.5);
    NodeLineSegment testSegment = new NodeLineSegment(point1, point2, RADIUS);
    final float TOTAL_DENSITY_1000 = 1000.0F;
    final int NUMBER_OF_CLOUD_POINTS = 1000;

    AxisAlignedBB aabbAll = new AxisAlignedBB(0.0, 0.5, 1.5, 5.0, 6.0, 9.0);
    AxisAlignedBB aabbNone = new AxisAlignedBB(0.0, 5.0, 8.0, 1.0, 5.5, 8.5);
    AxisAlignedBB aabbSome = new AxisAlignedBB(3.0, 2.0, 3.0, 4.0, 4.5, 7.5);

    float density = testSegment.collisionCheckAABB(aabbAll, TOTAL_DENSITY_1000, NUMBER_OF_CLOUD_POINTS);
    assertTrue(Math.abs(density - TOTAL_DENSITY_1000) < TOTAL_DENSITY_1000 / 10000.0F);

    density = testSegment.collisionCheckAABB(aabbNone, TOTAL_DENSITY_1000, NUMBER_OF_CLOUD_POINTS);
    assertTrue(density < TOTAL_DENSITY_1000 / 10000.0F);

    density = testSegment.collisionCheckAABB(aabbSome, TOTAL_DENSITY_1000, NUMBER_OF_CLOUD_POINTS);
    assertTrue(density > TOTAL_DENSITY_1000 / 10000.0F
               && Math.abs(density - TOTAL_DENSITY_1000) > TOTAL_DENSITY_1000 / 10000.0F);
  }

  @Test
  public void testGetAxisAlignedBoundingBox() throws Exception
  {
    final float RADIUS = 0.45F;
    Vec3 point1 = new Vec3(0.5, 1.0, 2.0);
    Vec3 point2 = new Vec3(4.5, 5.5, 8.5);
    NodeLineSegment testSegment = new NodeLineSegment(point1, point2, RADIUS);

    AxisAlignedBB aabb = testSegment.getAxisAlignedBoundingBox();
    AxisAlignedBB aabbResult = new AxisAlignedBB(point1.xCoord, point1.yCoord, point1.zCoord,
                                                 point2.xCoord, point2.yCoord, point2.zCoord);
    aabbResult = aabbResult.expand(RADIUS, RADIUS, RADIUS);
    assertTrue(Math.abs(aabb.minX - aabbResult.minX) < 0.001);
    assertTrue(Math.abs(aabb.minY - aabbResult.minY) < 0.001);
    assertTrue(Math.abs(aabb.minZ - aabbResult.minZ) < 0.001);
    assertTrue(Math.abs(aabb.maxX - aabbResult.maxX) < 0.001);
    assertTrue(Math.abs(aabb.maxY - aabbResult.maxY) < 0.001);
    assertTrue(Math.abs(aabb.maxZ - aabbResult.maxZ) < 0.001);
  }

  @Test
  public void testGetAxisAlignedBoundingBoxForAll() throws Exception
  {
    final float RADIUS = 0.45F;
    final int SEGMENT_COUNT = 20;
    Random random = new Random();
    ArrayList<NodeLineSegment> segments = new ArrayList<NodeLineSegment>();
    AxisAlignedBB union = null;
    for (int i = 0; i < SEGMENT_COUNT; ++i) {
      Vec3 point1 = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
      Vec3 point2 = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
      float radius = RADIUS * random.nextFloat();
      NodeLineSegment newSegment = new NodeLineSegment(point1, point2, radius);
      AxisAlignedBB aabb = new AxisAlignedBB(point1.xCoord, point1.yCoord, point1.zCoord,
                                             point2.xCoord, point2.yCoord, point2.zCoord);
      aabb = aabb.expand(radius, radius, radius);
      segments.add(newSegment);
      if (union == null) {
        union = aabb;
      } else {
        union = union.union(aabb);
      }
    }
    AxisAlignedBB result = NodeLineSegment.getAxisAlignedBoundingBoxForAll(segments);
    assertTrue(Math.abs(union.minX - result.minX) < 0.001);
    assertTrue(Math.abs(union.minY - result.minY) < 0.001);
    assertTrue(Math.abs(union.minZ - result.minZ) < 0.001);
    assertTrue(Math.abs(union.maxX - result.maxX) < 0.001);
    assertTrue(Math.abs(union.maxY - result.maxY) < 0.001);
    assertTrue(Math.abs(union.maxZ - result.maxZ) < 0.001);
  }

  //  @Test
//  public void testAddOverlappedCubes() throws Exception
//  {
//    HashSet<Vec3i> overlappedCubes = new HashSet<Vec3i>();
//    Vec3 point1 = new Vec3(0.5, 0.5, 0.5);
//    Vec3 point2 = new Vec3(4.5, 0.5, 0.5);
//    NodeLineSegment testSegment = new NodeLineSegment(point1, point2);
//    testSegment.addOverlappedCubes(overlappedCubes, 0.1);
//
//    // expect: [0,0,0], [1,0,0] ... [4,0,0]
//    List<Vec3i> allOverlaps = new ArrayList<Vec3i>(overlappedCubes);
//    assertTrue(allOverlaps.size() == 5);
//
//    HashSet<Vec3i> expected = new HashSet<Vec3i>();
//    expected.add(new Vec3i(0, 0, 0));
//    expected.add(new Vec3i(1, 0, 0));
//    expected.add(new Vec3i(2, 0, 0));
//    expected.add(new Vec3i(3, 0, 0));
//    expected.add(new Vec3i(4, 0, 0));
//
//    for (Vec3i entry : allOverlaps) {
//      assertTrue(expected.contains(entry));
//      expected.remove(entry);
//    }
//  }


  //  @Test
//  public void testSort() throws Exception
//  {
//    final int TEST_COUNT = 100;
//    Random random = new Random();
//    ArrayList<NodeLineSegment> array1 = generateRandom(random, TEST_COUNT);
//    NodeLineSegment.sort(array1, NodeLineSegment.SortOrder.X_LOW);
//    double lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array1) {
//      assertTrue(segment.startPoint.xCoord >= lastValue);
//      assertTrue(segment.startPoint.xCoord <= segment.endPoint.xCoord);
//      lastValue = segment.startPoint.xCoord;
//    }
//
//    ArrayList<NodeLineSegment> array2 = generateRandom(random, TEST_COUNT);
//    NodeLineSegment.sort(array2, NodeLineSegment.SortOrder.X_HIGH);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array2) {
//      assertTrue(segment.endPoint.xCoord >= lastValue);
//      assertTrue(segment.startPoint.xCoord <= segment.endPoint.xCoord);
//      lastValue = segment.endPoint.xCoord;
//    }
//
//    ArrayList<NodeLineSegment> array3 = generateRandom(random, TEST_COUNT);
//    NodeLineSegment.sort(array3, NodeLineSegment.SortOrder.Y_LOW);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array3) {
//      assertTrue(segment.startPoint.yCoord >= lastValue);
//      assertTrue(segment.startPoint.yCoord <= segment.endPoint.yCoord);
//      lastValue = segment.startPoint.yCoord;
//    }
//
//    ArrayList<NodeLineSegment> array4 = generateRandom(random, TEST_COUNT);
//    NodeLineSegment.sort(array4, NodeLineSegment.SortOrder.Y_HIGH);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array4) {
//      assertTrue(segment.endPoint.yCoord >= lastValue);
//      assertTrue(segment.startPoint.yCoord <= segment.endPoint.yCoord);
//      lastValue = segment.endPoint.yCoord;
//    }
//
//    ArrayList<NodeLineSegment> array5 = generateRandom(random, TEST_COUNT);
//    NodeLineSegment.sort(array5, NodeLineSegment.SortOrder.Z_LOW);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array5) {
//      assertTrue(segment.startPoint.zCoord >= lastValue);
//      assertTrue(segment.startPoint.zCoord <= segment.endPoint.zCoord);
//      lastValue = segment.startPoint.zCoord;
//    }
//
//    ArrayList<NodeLineSegment> array6 = generateRandom(random, TEST_COUNT);
//    NodeLineSegment.sort(array6, NodeLineSegment.SortOrder.Z_HIGH);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array6) {
//      assertTrue(segment.endPoint.zCoord >= lastValue);
//      assertTrue(segment.startPoint.zCoord <= segment.endPoint.zCoord);
//      lastValue = segment.endPoint.zCoord;
//    }
//
//
//    // sort a second time; no effect
//    NodeLineSegment.sort(array1, NodeLineSegment.SortOrder.X_LOW);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array1) {
//      assertTrue(segment.startPoint.xCoord >= lastValue);
//      assertTrue(segment.startPoint.xCoord <= segment.endPoint.xCoord);
//      lastValue = segment.startPoint.xCoord;
//    }
//
//    // resort in a different order
//    NodeLineSegment.sort(array1, NodeLineSegment.SortOrder.Z_HIGH);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array1) {
//      assertTrue(segment.endPoint.zCoord >= lastValue);
//      assertTrue(segment.startPoint.zCoord <= segment.endPoint.zCoord);
//      lastValue = segment.endPoint.zCoord;
//    }
//
//    // test deepCopy
//
//          // deepcopy sort matches original array1
//    ArrayList<NodeLineSegment> deepCopy = NodeLineSegment.deepCopy(array1);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : deepCopy) {
//      assertTrue(segment.endPoint.zCoord >= lastValue);
//      assertTrue(segment.startPoint.zCoord <= segment.endPoint.zCoord);
//      lastValue = segment.endPoint.zCoord;
//    }
//
//    // sort deepcopy and check correctly sorted
//    NodeLineSegment.sort(deepCopy, NodeLineSegment.SortOrder.Y_LOW);
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : deepCopy) {
//      assertTrue(segment.startPoint.yCoord >= lastValue);
//      assertTrue(segment.startPoint.yCoord <= segment.endPoint.yCoord);
//      lastValue = segment.startPoint.yCoord;
//    }
//
//        // original array still sorted properly as Z_HIGH
//    lastValue = Double.MIN_VALUE;
//    for (NodeLineSegment segment : array1) {
//      assertTrue(segment.endPoint.zCoord >= lastValue);
//      assertTrue(segment.startPoint.zCoord <= segment.endPoint.zCoord);
//      lastValue = segment.endPoint.zCoord;
//    }
//
//
//  }

}