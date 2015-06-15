/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.ModelRenderer;
import org.lwjgl.opengl.GL11;

public class ModelPartProxy {
    
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    
    public float renderScale = 1;
    
    public boolean hidden;
    
    private final ModelRenderer node;
    private final List<ModelPartProxy> childs;

    public ModelPartProxy(ModelRenderer node) {
        this.node = node;
        
        if (node.childModels != null) {
            childs = new ArrayList<ModelPartProxy>();
            for (Object childNode : node.childModels) {
                childs.add(new ModelPartProxy((ModelRenderer) childNode));
            }
        } else {
            childs = null;
        }
        
        update();
    }
    
    public void update() {
        rotationPointX = node.rotationPointX;
        rotationPointY = node.rotationPointY;
        rotationPointZ = node.rotationPointZ;

        rotateAngleX = node.rotateAngleX;
        rotateAngleY = node.rotateAngleY;
        rotateAngleZ = node.rotateAngleZ;

        hidden = node.isHidden;
        
        if (childs != null) {
            for (ModelPartProxy child : childs) {
                child.update();
            }
        }
    }
    
    protected void updatePart() {
        node.rotationPointX = rotationPointX;
        node.rotationPointY = rotationPointY;
        node.rotationPointZ = rotationPointZ;
        
        node.rotateAngleX = rotateAngleX;
        node.rotateAngleY = rotateAngleY;
        node.rotateAngleZ = rotateAngleZ;
        
        node.isHidden = hidden;
        
        if (childs != null) {
            for (ModelPartProxy child : childs) {
                child.updatePart();
            }
        }
    }
    
    public void render(float scale) {
        preScale();
        updatePart();
        node.render(scale / renderScale);
        postScale();
    }
    
    public void renderWithRotation(float scale) {
        preScale();
        updatePart();
        node.renderWithRotation(scale / renderScale);
        postScale();
    }
    
    public void postRender(float scale) {
        updatePart();
        node.postRender(scale);
    }

    private void preScale() {
        if (renderScale != 1) {
            GL11.glPushMatrix();
            GL11.glScalef(renderScale, renderScale, renderScale);
        }
    }
    
    private void postScale() {
        if (renderScale != 1) {
            GL11.glPopMatrix();
        }
    }
}
