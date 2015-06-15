/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon;

import info.ata4.minecraft.render.GLUtils;
import net.minecraft.src.Entity;
import net.minecraft.src.Render;

/**
 * Renderer for dragon hitboxes. Renders nothing unless debug options are
 * enabled.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonPartRenderer extends Render {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
        DragonPart part = (DragonPart) entity;
        
        if (part.base.renderHitbox) {
            GLUtils.renderAABB(entity.boundingBox,
                    x - entity.lastTickPosX,
                    y - entity.lastTickPosY,
                    z - entity.lastTickPosZ);
        }
    }

    @Override
    public void doRenderShadowAndFire(Entity entity, double x, double y, double z, float f, float f1) {
    }
}
