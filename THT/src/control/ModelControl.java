/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author hannes
 */
public class ModelControl extends AbstractControl{
    private static final float TIME_TO_CHECK = 1.0f;
    private static final int MAX_DISTANCE = 15;
    
    private Node playersNode;
    private float time = 0;
    
    public ModelControl(Node playersNode) {
        this.playersNode = playersNode;
    }
    
    
    
    @Override
    protected void controlUpdate(float tpf) {
        time += tpf;
        if (time > TIME_TO_CHECK) {
            time = 0;
            for (Spatial spatial : playersNode.getChildren()) {
                ((EntityNode) spatial).changeModel(getSpatial().getWorldTranslation(), MAX_DISTANCE);
            }
        }
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
}
