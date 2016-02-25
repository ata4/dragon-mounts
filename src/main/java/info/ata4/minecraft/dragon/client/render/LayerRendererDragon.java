/*
** 2016 February 23
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.render;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class LayerRendererDragon implements LayerRenderer<EntityTameableDragon> {
    
    protected final DragonRenderer dragonRenderer;

    public LayerRendererDragon(DragonRenderer dragonRenderer) {
        this.dragonRenderer = dragonRenderer;
    }
    
}
