/*
** 2011 December 21
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.render;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * Collection of some rendering functions used for debugging.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GLUtils {
    
    public static void renderAABB(AxisAlignedBB aabb, double ox, double oy, double oz) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        float alpha = 0.5f;
        
        GL11.glColor4f(1, 1, 1, alpha);
        
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setTranslationD(ox, oy, oz);
        tessellator.setNormal(0, 0, -1);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        tessellator.setNormal(0, 0, 1);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.setNormal(0, -1, 0);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        tessellator.setNormal(0, 1, 0);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.setNormal(-1, 0, 0);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        tessellator.setNormal(1, 0, 0);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        tessellator.setTranslationD(0, 0, 0);
        tessellator.draw();
        
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
    }
    
    public static void renderAABB(AxisAlignedBB aabb) {
        renderAABB(aabb, 0, 0, 0);
    }
    
    public static void renderAxes(double x, double y, double z, float size) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glBegin(GL11.GL_LINES);
        
        float alpha = 0.75f;
        
        GL11.glColor4f(1, 0, 0, alpha);
        GL11.glVertex3d(x - size, y, z);
        GL11.glVertex3d(x + size, y, z);
        
        GL11.glColor4f(0, 1, 0, alpha);
        GL11.glVertex3d(x, y - size, z);
        GL11.glVertex3d(x, y + size, z);
        
        GL11.glColor4f(0, 0, 1, alpha);
        GL11.glVertex3d(x, y, z - size);
        GL11.glVertex3d(x, y, z + size);
        
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnd();
        
        GL11.glDisable(GL11.GL_BLEND);
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
    }
    
    public static void renderAxes(double x, double y, double z) {
        renderAxes(x, y, z, 1);
    }
    
    public static void renderAxes(float size) {
        renderAxes(0, 0, 0, size);
    }
    
    public static void renderAxes() {
        renderAxes(0, 0, 0, 1);
    }
}
