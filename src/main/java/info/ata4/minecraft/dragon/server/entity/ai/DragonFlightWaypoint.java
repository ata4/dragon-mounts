/*
 ** 2013 July 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

/**
 * Generic flight waypoint for dragons.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonFlightWaypoint {
    
    private static final String NBT_WAYPOINT_X = "Waypoint-X";
    private static final String NBT_WAYPOINT_Y = "Waypoint-Y";
    private static final String NBT_WAYPOINT_Z = "Waypoint-Z";
    
    public int posX;
    public int posY;
    public int posZ;
    
    private final Entity entity;
    
    public DragonFlightWaypoint(Entity entity) {
        this.entity = entity;
    }
    
    public void set(int x, int y, int z) {
        posX = x;
        posY = y;
        posZ = z;
    }
    
    public void set(double x, double y, double z) {
        posX = (int) x;
        posY = (int) y;
        posZ = (int) z;
    }
    
    public void setBlockPos(BlockPos bp) {
        posX = bp.getX();
        posY = bp.getY();
        posZ = bp.getZ();
    }
    
    public BlockPos toBlockPos() {
        return new BlockPos(posX, posY, posZ);
    }

    public void setVector(Vec3 vec) {
        posX = (int) vec.xCoord;
        posY = (int) vec.yCoord;
        posZ = (int) vec.zCoord;
    }
    
    public void setEntity(Entity target) {
        posX = (int) target.posX;
        posY = (int) target.posY;
        posZ = (int) target.posZ;
    }
    
    public Vec3 toVector() {
        return new Vec3(posX, posY, posZ);
    }
    
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger(NBT_WAYPOINT_X, posX);
        nbt.setInteger(NBT_WAYPOINT_Y, posY);
        nbt.setInteger(NBT_WAYPOINT_Z, posZ);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        posX = nbt.getInteger(NBT_WAYPOINT_X);
        posY = nbt.getInteger(NBT_WAYPOINT_Y);
        posZ = nbt.getInteger(NBT_WAYPOINT_Z);
    }
    
    public double getDeltaX() {
        return posX - entity.posX;
    }
    
    public double getDeltaY() {
        return posY - entity.posY;
    }
    
    public double getDeltaZ() {
        return posZ - entity.posZ;
    }
    
    public double getDistanceSquare() {
        double deltaX = getDeltaX();
        double deltaY = getDeltaY();
        double deltaZ = getDeltaZ();
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }
    
    public double getDistance() {
        return Math.sqrt(getDistanceSquare());
    }
    
    public boolean isNear() {
        return getDistanceSquare() < entity.width * entity.width;
    }

    public void clear() {
        posX = (int) entity.posX;
        posY = (int) entity.posY;
        posZ = (int) entity.posZ;
    }

    @Override
    public String toString() {
        return posX + ", " + posY + ", " + posZ;
    }
}
