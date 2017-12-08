 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.models;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author hannes
 */
public class Player {
    private EntityType type;
    private Vector3f position;
    private Quaternion rotation;
    private int id;

    public Player(EntityType type, Vector3f position, Quaternion rotation, int id) {
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.id = id;
    }
    
    
    
    public void setType(EntityType type){
        this.type = type;
    }
    
    public EntityType getType(){
        return type;
    }
    
    
}
