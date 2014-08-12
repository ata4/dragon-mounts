/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for a model part that is used to project one model renderer on multiple
 * visible instances.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ModelPartProxy {
    
    // scale multiplier
    private float renderScaleX = 1;
    private float renderScaleY = 1;
    private float renderScaleZ = 1;
    
    // rotation points
    private float rotationPointX;
    private float rotationPointY;
    private float rotationPointZ;
    
    // rotation angles
    private float preRotateAngleX;
    private float preRotateAngleY;
    private float preRotateAngleZ;
    
    private float rotateAngleX;
    private float rotateAngleY;
    private float rotateAngleZ;
    
    // misc meta data
    private boolean hidden;
    private boolean showModel;

    // projected parts and part childs
    private final ModelPart part;
    private final List<ModelPartProxy> childs;
    
    /**
     * Constructs a new proxy for the given model part.
     * 
     * @param part model part to project on this proxy
     */
    public ModelPartProxy(ModelPart part) {
        this.part = part;
        
        if (part.childModels != null) {
            childs = new ArrayList<ModelPartProxy>();
            for (Object childModel : part.childModels) {
                childs.add(new ModelPartProxy((ModelPart) childModel));
            }
        } else {
            childs = null;
        }
        
        update();
    }
    
    /**
     * Saves the properties of the model part to this proxy with the default
     * rendering scale.
     */
    public final void update() {
        renderScaleX = part.renderScaleX;
        renderScaleY = part.renderScaleY;
        renderScaleZ = part.renderScaleZ;
        
        rotationPointX = part.rotationPointX;
        rotationPointY = part.rotationPointY;
        rotationPointZ = part.rotationPointZ;
        
        preRotateAngleX = part.preRotateAngleX;
        preRotateAngleY = part.preRotateAngleY;
        preRotateAngleZ = part.preRotateAngleZ;

        rotateAngleX = part.rotateAngleX;
        rotateAngleY = part.rotateAngleY;
        rotateAngleZ = part.rotateAngleZ;

        hidden = part.isHidden;
        showModel = part.showModel;

        if (childs != null) {
            for (ModelPartProxy child : childs) {
                child.update();
            }
        }
    }
    
    /**
     * Restores the properties from this proxy to the model part.
     */
    public final void apply() {
        part.renderScaleX = renderScaleX;
        part.renderScaleY = renderScaleY;
        part.renderScaleZ = renderScaleZ;
        
        part.rotationPointX = rotationPointX;
        part.rotationPointY = rotationPointY;
        part.rotationPointZ = rotationPointZ;
        
        part.preRotateAngleX = preRotateAngleX;
        part.preRotateAngleY = preRotateAngleY;
        part.preRotateAngleZ = preRotateAngleZ;
        
        part.rotateAngleX = rotateAngleX;
        part.rotateAngleY = rotateAngleY;
        part.rotateAngleZ = rotateAngleZ;
        
        part.isHidden = hidden;
        part.showModel = showModel;
        
        if (childs != null) {
            for (ModelPartProxy child : childs) {
                child.apply();
            }
        }
    }
    
    public void render(float scale) {
        apply();
        part.render(scale);
    }
}
