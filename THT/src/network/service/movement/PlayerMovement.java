/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author ted
 */
@Serializable
public class PlayerMovement {
    
    public String id;
    public Vector3f location;
    public Vector3f direction;
    public Vector3f rotation;
    
    public PlayerMovement(){
    }
    
    public PlayerMovement(String id, Vector3f location, Vector3f direction, Vector3f rotation){
        this.id = id;
        this.location = location;
        this.direction = direction;
        this.rotation = rotation;
    }
   
}
