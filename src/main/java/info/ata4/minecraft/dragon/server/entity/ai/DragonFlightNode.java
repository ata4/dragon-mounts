/*
 ** 2013 May 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * An iterative A* shortest path searching using randomized paths
 * 
 * @author KotBehemoth
 */
public class DragonFlightNode {

    public static final int NUM_CHILDREN = 5;
    public static final double MIN_SAMPLE_DISTANCE = 3; // Keeps samples some distance away from each other. Otherwise dragon could fly in circle around itself
    
    private Random rand = new Random();
    public DragonFlightNode[] children = new DragonFlightNode[NUM_CHILDREN]; // Monte Carlo estimates
    public DragonFlightNode parent;
    
    public int myIdx;
    public boolean hasAlternatives;     // count of non null children > 1, will be later used by backtracking
    
    public double pointX;
    public double pointY;
    public double pointZ;
    
    public double endPointX;            // became useless now
    public double endPointY;
    public double endPointZ;
    
    public double sourceDistance;       // Distance to root node (start point)
    public double targetDistance;       // Distance to destination
    public double blockDistance;        // Distance to occlusion between current node and destination
    
    int idxToFastestWay;                // Which branch leads to destination
    public double fastestSubDistance;   // Estimation of how much far the dragon should travel at minimum

    DragonFlightNode(Vec3 source) {
        this.parent = null;
        this.myIdx = -1;

        this.pointX = source.xCoord;
        this.pointY = source.yCoord;
        this.pointZ = source.zCoord;

        this.sourceDistance = 0;
        this.targetDistance = 0;
        this.blockDistance = 9999;

        this.endPointX = source.xCoord;
        this.endPointY = source.yCoord;
        this.endPointZ = source.zCoord;

        this.hasAlternatives = false;
        
        collapseAll();
    }

    DragonFlightNode(DragonFlightNode parent, int myIdx, Vec3 source) {
        this.parent = parent;
        parent.children[myIdx] = this;
        this.myIdx = myIdx;

        this.pointX = source.xCoord;
        this.pointY = source.yCoord;
        this.pointZ = source.zCoord;

        Vec3 parentSource = new Vec3(parent.pointX, parent.pointY, parent.pointZ);
        this.sourceDistance = parent.sourceDistance + parentSource.distanceTo(source);
        this.targetDistance = 0;
        this.blockDistance = 999999;

        this.endPointX = source.xCoord;
        this.endPointY = source.yCoord;
        this.endPointZ = source.zCoord;

        this.hasAlternatives = false;
        
        collapseAll();
    }

    // gets an estimated distance to the end target
    public double getHeuristicalDistance() {
        if (idxToFastestWay != -1) {
            return sourceDistance + fastestSubDistance;
        } else {
            return sourceDistance + targetDistance;        // should work as estimate because of triangle inequality
        }
    }

    // Will be called if any descendants changed
    private void updateSubCandidates() {
        int countNonNull = 0;
        int i;

        // do an overall comparison and determine which path is the best one
        double fastestSumDistance = 9999999;
        int idxToFastestWay = -1;

        for (i = 0; i < NUM_CHILDREN; i++) {
            DragonFlightNode subNode = children[i];
            if (subNode != null) {
                double otherSumDistance = subNode.getHeuristicalDistance();
                if (fastestSumDistance > otherSumDistance) {
                    fastestSumDistance = otherSumDistance;
                    idxToFastestWay = i;
                }

                countNonNull++;
            }
        }

        // update path values
        this.fastestSubDistance = fastestSumDistance - sourceDistance;
        this.idxToFastestWay = idxToFastestWay;

        if (countNonNull > 1) {
            hasAlternatives = true;
        } else {
            hasAlternatives = false;
        }

        if (parent != null) {
            parent.updateSubCandidates(); // update parent
        }
    }

    // Will be called after "orphanizeMe" to recalculate source distance over the whole subtree
    private void recalculateSourceDistance() {
        if (parent != null) {
            Vec3 parentSource = new Vec3(parent.pointX, parent.pointY, parent.pointZ);
            Vec3 source = new Vec3(pointX, pointY, pointZ);
            this.sourceDistance = parent.sourceDistance + parentSource.distanceTo(source);
        } else {
            this.sourceDistance = 0;
        }

        for (int i = 0; i < NUM_CHILDREN; i++) {
            DragonFlightNode subNode = children[i];
            if (subNode != null) {
                subNode.recalculateSourceDistance();
            }
        }
    }

    // Used before launching explore() to reset source distance
    public void orphanizeMe() {
        if (parent == null) {
            return;
        }

        // remove me from parent
        parent.children[myIdx] = null;

        // pick another candidate at parent
        parent.updateSubCandidates();

        // clear parent linkage
        parent = null;
        myIdx = -1;

        // recalculate sourceDistances
        recalculateSourceDistance();
    }

    // Can be called to remove unused nodes to save memory.
    public void eliminateSubOptimalPaths() {
        for (int i = 0; i < NUM_CHILDREN; i++) {
            DragonFlightNode subNode = children[i];
            if (subNode != null && i != idxToFastestWay) {
                subNode.orphanizeMe();
            }
        }

        hasAlternatives = false;
    }

    // Remove all children
    public void collapseAll() {
        int i;

        for (i = 0; i < NUM_CHILDREN; i++) {
            DragonFlightNode subNode = children[i];
            if (subNode != null) {
                subNode.parent = null;
                subNode.sourceDistance = 0;
                children[i] = null;
            }
        }

        idxToFastestWay = -1;
        fastestSubDistance = this.targetDistance + 9999999;

        hasAlternatives = false;

        if (parent != null) {
            parent.updateSubCandidates(); // update parent
        }
    }

    // Collision detection like rayTrace, but tests the whole bounding box of dragon instead of a ray
    // NOTE: Doesn't work perfect. Can sometimes bring problems with landing
    //         
    private double simulateTunnelMovement(EntityCreature creature, Vec3 target) {
        // simulates the dragon movement inside a straight virtual tunnel as if there would be repeated "moveAndResizeEntity" calls
        // It is better than ray collision, so the dragon keeps distance to surrounding blockers.
        /////

        final double stepSize = 1;                // simulation distance step. 1 should be best. The more the worse performance and quality
        final double tunnelWallDistance = 1.0;    // dragon will keep a distance of a few blocks to walls when flying. 1.0 for one block
        World worldObj = creature.worldObj;

        Vec3 source = new Vec3(pointX, pointY, pointZ);
        Vec3 delta = new Vec3(target.xCoord - source.xCoord, target.yCoord - source.yCoord, target.zCoord - source.zCoord);
        double deltaLen = delta.lengthVector();

        // find largest coordinate entry
        int largestCoord = -1;
        if (Math.abs(delta.xCoord) >= Math.abs(delta.yCoord)) {
            if (Math.abs(delta.xCoord) >= Math.abs(delta.zCoord)) {
                largestCoord = 0;
            } else {
                largestCoord = 2;
            }
        } else {
            if (Math.abs(delta.yCoord) >= Math.abs(delta.zCoord)) {
                largestCoord = 1;
            } else {
                largestCoord = 2;
            }
        }

        // Normalize delta with infinite-Norm
        double maxCoord = 0;
        if (largestCoord == 0) {
            maxCoord = Math.abs(delta.xCoord);
        } else if (largestCoord == 1) {
            maxCoord = Math.abs(delta.yCoord);
        } else {
            maxCoord = Math.abs(delta.zCoord);
        }
        if (maxCoord <= 0.0001) {
            return 99999999;
        }

        Vec3 stepDelta = new Vec3(delta.xCoord * stepSize / maxCoord, delta.yCoord * stepSize / maxCoord, delta.zCoord * stepSize / maxCoord);
        double stepDeltaLen = stepDelta.lengthVector();

        // make a proxy area
        AxisAlignedBB proxyInnerBox = creature.getEntityBoundingBox().expand(0,0,0);  // equivalent substitute for copy()
        Vec3 center = new Vec3((proxyInnerBox.minX + proxyInnerBox.maxX) / 2, (proxyInnerBox.minY + proxyInnerBox.maxY) / 2, (proxyInnerBox.minZ + proxyInnerBox.maxZ) / 2);
        proxyInnerBox.offset(pointX - center.xCoord, pointY - center.yCoord, pointZ - center.zCoord);

        AxisAlignedBB proxyBox = proxyInnerBox.expand(tunnelWallDistance, tunnelWallDistance, tunnelWallDistance);
        proxyBox = proxyInnerBox.fromBounds(proxyBox.minX, proxyBox.minY - 1.5, proxyBox.minZ,
                                            proxyBox.maxX, proxyBox.maxY + 0.5, proxyBox.maxZ);
//        proxyBox.maxY += 0.5;    // ceiling problem, where the dragon would take damage
//        proxyBox.minY -= 1.5;    // prevent early landing

        // test for initial collisions, reject simulation if there already collisions and the simulation rules towards them
        boolean tunnelCollided = false;
        List collidersInside;
        int i, j;
        double stepX, stepY, stepZ;

        stepX = stepDelta.xCoord;
        stepY = stepDelta.yCoord;
        stepZ = stepDelta.zCoord;
        collidersInside = worldObj.getCollidingBoundingBoxes(creature, proxyBox.addCoord(stepX, stepY, stepZ));

        for (j = 0; j < collidersInside.size(); j++) {
            AxisAlignedBB other = (AxisAlignedBB) collidersInside.get(j);
            if (!other.intersectsWith(proxyBox)) {
                continue;
            }

            if (other.minX <= proxyBox.maxX && stepX > 0) {
                tunnelCollided = true;    // blocked
            } else if (other.maxX >= proxyBox.minX && stepX < 0) {
                tunnelCollided = true;    // blocked
            }
            if (other.minY <= proxyBox.maxY && stepY > 0) {
                tunnelCollided = true;    // blocked
            } else if (other.maxY >= proxyBox.minY && stepY < 0) {
                tunnelCollided = true;    // blocked
            }
            if (other.minZ <= proxyBox.maxZ && stepZ > 0) {
                tunnelCollided = true;    // blocked
            } else if (other.maxZ >= proxyBox.minZ && stepZ < 0) {
                tunnelCollided = true;    // blocked
            }
            if (tunnelCollided) {
                break;
            }
        }

        // perform a simulation
        ///////

        double movedLen = 0;
        if (!tunnelCollided) {
            int numSteps = MathHelper.floor_double(maxCoord / stepSize + 1);

            for (i = 0; i < numSteps; i++) {
                stepX = stepDelta.xCoord;
                stepY = stepDelta.yCoord;
                stepZ = stepDelta.zCoord;
                collidersInside = worldObj.getCollidingBoundingBoxes(creature, proxyBox.addCoord(stepX, stepY, stepZ));

                // move box and test for collisions
                for (j = 0; j < collidersInside.size(); j++) {
                    stepY = ((AxisAlignedBB) collidersInside.get(j)).calculateYOffset(proxyBox, stepY);
                }
                proxyBox.offset(0, stepY, 0);
                proxyInnerBox.offset(0, stepY, 0);

                for (j = 0; j < collidersInside.size(); j++) {
                    stepX = ((AxisAlignedBB) collidersInside.get(j)).calculateXOffset(proxyBox, stepX);
                }
                proxyBox.offset(stepX, 0, 0);
                proxyInnerBox.offset(stepX, 0, 0);

                for (j = 0; j < collidersInside.size(); j++) {
                    stepZ = ((AxisAlignedBB) collidersInside.get(j)).calculateZOffset(proxyBox, stepZ);
                }
                proxyBox.offset(0, 0, stepZ);
                proxyInnerBox.offset(0, 0, stepZ);

                // test if tunnel collided
                if (stepX != stepDelta.xCoord || stepY != stepDelta.yCoord || stepZ != stepDelta.zCoord) {
                    movedLen += new Vec3(stepX, stepY, stepZ).lengthVector();

                    // for moving the inner part
                    stepX = stepDelta.xCoord - stepX;
                    stepY = stepDelta.yCoord - stepY;
                    stepZ = stepDelta.zCoord - stepZ;

                    tunnelCollided = true;
                    break;    // end simulation
                } else {
                    movedLen += stepDeltaLen;
                }
            }
        }

        // try to move inner part a little bit more, so dragon would be able to land 
        if (tunnelCollided) {
            for (j = 0; j < collidersInside.size(); j++) {
                stepY = ((AxisAlignedBB) collidersInside.get(j)).calculateYOffset(proxyInnerBox, stepY);
            }
            proxyInnerBox.offset(0, stepY, 0);

            for (j = 0; j < collidersInside.size(); j++) {
                stepX = ((AxisAlignedBB) collidersInside.get(j)).calculateXOffset(proxyInnerBox, stepX);
            }
            proxyInnerBox.offset(stepX, 0, 0);

            for (j = 0; j < collidersInside.size(); j++) {
                stepZ = ((AxisAlignedBB) collidersInside.get(j)).calculateZOffset(proxyInnerBox, stepZ);
            }
            proxyInnerBox.offset(0, 0, stepZ);

            movedLen += new Vec3(stepX, stepY, stepZ).lengthVector();
        }

        // tell if could move fully
        if (movedLen + 0.5 > deltaLen) {
            return 999999999;    // say, that no collision occured
        }
        return movedLen;
    }

    // Is called by explore() to check if the could be a direct way to target or not.
    private void traceTarget(EntityCreature creature, Vec3 target) {
        Vec3 source = new Vec3(pointX, pointY, pointZ);
        targetDistance = source.distanceTo(target);

        // make a raytrace to find if it is a direct way
        blockDistance = simulateTunnelMovement(creature, target); //*/ multisampleRayTrace( creature, target );

        // clear children and update candidate
        collapseAll();
    }

    // Searches for a good path to some given target point. CountNodes specify the amount of nodes which should be visited maximally
    public void explore(EntityCreature creature, Vec3 target, int countNodes) {
        // An iterative A* shortest path algorithm using monte carlo simulation to create
        // navigation waypoint candidates

        // test path
        traceTarget(creature, target);

        double switchCoeff = 86.79; //104.91; // simulated anealing temperature = exp(-switchCoeff * sampleIndex)
        // 86,79 for 3% probability at 5th node expansion
        if (countNodes > 1) {
            switchCoeff /= (double) (countNodes - 1);
        }

        // clear children
        collapseAll();

        int i;
        for (i = 0; i < countNodes; i++) {
            // traverse best path
            DragonFlightNode bestNode = this;
            while (true) {
                DragonFlightNode nextNode = bestNode.pickBest();
                if (nextNode == null) {
                    break;
                }
                bestNode = nextNode;
            }

            Vec3 delta = new Vec3(target.xCoord - bestNode.pointX,
                    target.yCoord - bestNode.pointY,
                    target.zCoord - bestNode.pointZ);
            double deltaLength = delta.lengthVector();

            // if blocked, then generate new children by monte carlo using a direction
            if (!bestNode.isDirectWay()) //NOUSE && deltaLength >= 3 )
            {
                // create a few monte carlo estimates
                int j;
                DragonFlightNode subNode;
                for (j = 0; j < NUM_CHILDREN; j++) {
                    // generate a random direction from "bestNode" with a drift away from ancestors by some chance to improve variety
                    Vec3 direction; // = Vec3.createVector( 0, 0, 0 );
                    double ancDirX, ancDirY, ancDirZ;
                    double switchProb;
                    boolean isInvalid = false;
                    DragonFlightNode ancNode;
                    
                    do {
                        isInvalid = false;
                        direction = new Vec3(2 * rand.nextDouble() - 1, 2 * rand.nextDouble() - 1, 2 * rand.nextDouble() - 1);

                        // drop a sample which is tracing back to ancestors area
                        ancNode = bestNode.parent;
                        while (ancNode != this.parent && ancNode != null) {
                            // cancel ancestor search to restrict on local branches by some probability
                            // drifted sampling is better when it comes to search more straightforward but it is too vulnerable to initial setup of ancestors
                            // this could drive the dragon away from target by a high chance. While pure random walk looks like the dragon got alcoholized or undecided in certain way
                            switchProb = Math.exp(-(double) i * switchCoeff);
                            if (rand.nextDouble() >= switchProb) {
                                break;
                            }

                            ancDirX = bestNode.pointX - ancNode.pointX;
                            ancDirY = bestNode.pointY - ancNode.pointY;
                            ancDirZ = bestNode.pointZ - ancNode.pointZ;

                            if (direction.xCoord * ancDirX + direction.yCoord * ancDirY + direction.zCoord * ancDirZ < 0) {
                                isInvalid = true;
                                break;
                            }

                            ancNode = ancNode.parent;
                        }
                    } while (isInvalid);

                    direction = direction.normalize();

                    double subTargetDistance = deltaLength;
                    if (subTargetDistance > 50) {
                        subTargetDistance = 50;
                    }
                    subTargetDistance *= (0.5 + rand.nextDouble() * 0.5);

                    // Test direction for collision
                    Vec3 subTarget = new Vec3(bestNode.pointX + direction.xCoord * subTargetDistance,
                            bestNode.pointY + direction.yCoord * subTargetDistance,
                            bestNode.pointZ + direction.zCoord * subTargetDistance);
                    double subBlockDistance = bestNode.simulateTunnelMovement(creature, subTarget);
                    if (subBlockDistance < subTargetDistance) {
                        subBlockDistance *= (3 / 4);
                        if (subBlockDistance < MIN_SAMPLE_DISTANCE) {
                            continue;    // reject small samples. Otherwise the dragon would fly around itself. ANNNNNOYING
                        }
                        subTarget = new Vec3(bestNode.pointX + direction.xCoord * subBlockDistance,
                                bestNode.pointY + direction.yCoord * subBlockDistance,
                                bestNode.pointZ + direction.zCoord * subBlockDistance);
                    }

                    subNode = new DragonFlightNode(bestNode, j, subTarget);
                    subNode.traceTarget(creature, target);
                }
            } else {
                // just tie a line to it
                bestNode = new DragonFlightNode(bestNode, 0, target);
                return;        // The A* search resulted in success, no better path can be found actually within previously given monte carlo estimates
            }
        }
    }

    // Is it the final node?
    public boolean isFinal() {
        return (this.targetDistance <= MIN_SAMPLE_DISTANCE);
    }

    //Is it the node which leads to final node?
    public boolean isDirectWay() {
        return (this.blockDistance + 3 > this.targetDistance);
    }

    // Gives out the best estimated path
    public DragonFlightNode pickBest() {
        return (idxToFastestWay != -1) ? children[idxToFastestWay] : null;
    }
}