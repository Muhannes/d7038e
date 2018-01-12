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
    public Vector3f nextDirection;

    public MyCharacterControl(CollisionShape shape, float stepHeight) {
        super(shape, stepHeight);
    }
    
    public void setMovementSpeed(float movementSpeed){
        this.movementSpeed = movementSpeed;
    }
    
    public synchronized void setNextDirection(Vector3f dir){
        nextDirection = dir;
    }
    
    public synchronized Vector3f getNextDirection(){
        if (nextDirection != null) {
            Vector3f tmpDir = nextDirection.clone();
            nextDirection = null;
            return tmpDir;
        } else {
            return null;
        }
        
    }
    
    
    @Override
    public void update(float tpf){
        Vector3f newDir = getNextDirection();
        if (newDir != null) {
            setWalkDirection(newDir);
        }
        //float movementSpeed = ((EntityNode) getSpatial()).movementSpeed;
        Vector3f newWalkDir = getWalkDirection().normalize().mult(movementSpeed).mult(tpf);
        setWalkDirection(newWalkDir);
        super.update(tpf);
    }
    
}
