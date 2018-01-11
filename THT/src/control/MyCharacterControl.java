/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;

/**
 *
 * @author hannes
 */
public class MyCharacterControl extends CharacterControl {
    public float movementSpeed;

    public MyCharacterControl(CollisionShape shape, float stepHeight) {
        super(shape, stepHeight);
    }
    
    public void setMovementSpeed(float movementSpeed){
        this.movementSpeed = movementSpeed;
    }
    
    
    @Override
    public void update(float tpf){
        //float movementSpeed = ((EntityNode) getSpatial()).movementSpeed;
        Vector3f newWalkDir = getWalkDirection().normalize().mult(movementSpeed).mult(tpf);
        setWalkDirection(newWalkDir);
        super.update(tpf);
    }
    
}
