/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.trap;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Traps
 * 
 * @author hannes
 */
public abstract class Trap extends Geometry {
    private TrapType type;
    GhostControl ghost;

    public Trap(Vector3f pos, TrapType type) {
        super("Trap");
        this.type = type;
        initTrap(pos);
    }
    
    private void initTrap(Vector3f pos){
        Vector3f trapSize = new Vector3f(0.1f,0.1f,0.1f);
        ghost = new GhostControl(
            new BoxCollisionShape(trapSize));  // a box-shaped ghost
        setLocalTranslation(pos);
        Box box = new Box(trapSize.x, trapSize.y, trapSize.z);
        setMesh(box);
        addControl(ghost);
    }
    
    public TrapType getType(){
        return type;
    }
    
    public void addToPhysicsSpace(BulletAppState bulletAppState){
        bulletAppState.getPhysicsSpace().add(ghost);
    }
    
    public static void applyEffect(CharacterControl character){
        throw new UnsupportedOperationException("You need to override this method in Trap class.");
        // Override this method in Traps
    }
}
