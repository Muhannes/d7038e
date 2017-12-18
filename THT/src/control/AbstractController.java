/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.input.InputManager;
import com.jme3.scene.Spatial;

/**
 *
 * @author ted
 */
public abstract class AbstractController {
    
    protected Spatial entity;
    
    public void setEntity(Spatial entity){
        this.entity = entity;
    }
    
    public abstract void initKeys(InputManager manager);
}
