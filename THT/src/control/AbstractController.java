/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import api.models.Entity;
import com.jme3.input.InputManager;

/**
 *
 * @author ted
 */
public abstract class AbstractController {
    
    Entity entity;
    
    public void setEntity(Entity entity){
        this.entity = entity;
    }
    
    public abstract void initKeys(InputManager manager);
}
