/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ted
 */
public class TrapController extends GhostControl implements PhysicsCollisionListener, PhysicsTickListener{

    private static final Logger LOGGER = Logger.getLogger(TrapController.class.getName());
    
    private final BulletAppState bulletAppState;
    private final Node root;
            
    public TrapController(BulletAppState bullet, Node root) {
        this.bulletAppState = bullet;
        this.root = root;
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }

    /*
    * TODO: Somehow update the tree so that the traps triggered are removed.
    * TODO: Somehow update the players so that they are slowed when triggering traps.
    */
    @Override
    public void collision(PhysicsCollisionEvent event) {
        if(!event.getNodeA().getName().equals("Quad") && !event.getNodeB().getName().equals("Quad")){   
            if(event.getNodeA().getParent().getName().equals("playersNode") && event.getNodeB().getParent().getParent().getName().equals("traps")){
                LOGGER.log(Level.INFO, "New Collision " + event.getNodeA().getName() + " - " + event.getNodeB().getName());
            /*    root.detachChildNamed(event.getNodeB().getParent().getName());
                if(root.getChild(event.getNodeB().getParent().getName()) == null){
                    LOGGER.log(Level.SEVERE, "Trap " + event.getNodeB().getParent().getName() + " was not removed");
                }*/
            } else if(event.getNodeB().getParent().getName().equals("playersNode") && event.getNodeA().getParent().getParent().getName().equals("traps")){
                LOGGER.log(Level.INFO, "New Collision " + event.getNodeA().getName() + " - " + event.getNodeB().getName());
                /*root.detachChildNamed(event.getNodeB().getParent().getName());
                if(root.getChild(event.getNodeA().getParent().getName()) != null){
                    LOGGER.log(Level.SEVERE, "Trap " + event.getNodeA().getParent().getName() + " was not removed");
                }*/
            } else if(event.getNodeA().getParent().getName().equals("playersNode") && event.getNodeB().getParent().getName().equals("playersNode")){
                /* Check if any of the two are monster, and if so, the other is killed */
                EntityNode player1 = (EntityNode) root.getChild(event.getNodeA().getName());
                EntityNode player2 = (EntityNode) root.getChild(event.getNodeB().getName());
                LOGGER.log(Level.INFO, "Collision between players");
            }
        }
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        System.out.println("prePhysicsTick");
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        System.out.println("PhysicsTick");
    }
}
