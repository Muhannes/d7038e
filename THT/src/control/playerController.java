/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.BetterCharacterControl;

/**
 * This is a controller that notices when two players collide ( dies, nothing ) etc
 * @author ted
 */
public class playerController extends BetterCharacterControl implements PhysicsCollisionListener{

    @Override
    public void collision(PhysicsCollisionEvent event) {
        
        
    }
    
}
