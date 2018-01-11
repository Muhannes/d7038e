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

    private Node playersNode;
    
    public ModelControl(Node playersNode) {
        this.playersNode = playersNode;
    }
    
    
    
    @Override
    protected void controlUpdate(float tpf) {
       // for (Spatial spatial : playersNode.getChildren()) {
       //     ((EntityNode) spatial).changeModel(getSpatial().getWorldTranslation());
       // }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
}
