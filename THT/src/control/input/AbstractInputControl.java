/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.input;

import com.jme3.input.InputManager;
import com.jme3.input.controls.AnalogListener;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 * Base class for Human and Monster input control
 * 
 * @author truls
 */
public abstract class AbstractInputControl extends AbstractControl implements AnalogListener{

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // Nothing
    }
    
    /**
     * Set up keybindings
     * @param manager 
     */
    public abstract void initKeys(InputManager manager);
    
}
