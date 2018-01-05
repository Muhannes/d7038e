/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.audio;

import com.jme3.audio.Listener;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * This controll act as the ears of the player in the game.
 * 
 * @author truls
 */
public class ListenerControl extends AbstractControl{
    
    private Listener listener;
    
    public ListenerControl(Listener listener){
        this.listener = listener;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // Move the listener along with the players spatial
        listener.setLocation(getSpatial().getWorldTranslation());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nothing
    }
    
    
}
