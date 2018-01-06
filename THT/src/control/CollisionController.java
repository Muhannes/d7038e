/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.GhostControl;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.gameserver.PlayState;
import network.service.gamestats.server.HostedGameStatsService;

/**
 *
 * @author ted
 */
public class CollisionController extends GhostControl implements PhysicsCollisionListener{

    private static final Logger LOGGER = Logger.getLogger(CollisionController.class.getName());
    
    private final BulletAppState bulletAppState;
    private final Node root;
    private final HostedGameStatsService hostedGameStatsService;
    private PlayState playState;
    private List<String> triggeredTraps = new ArrayList<>();
    
    public CollisionController(PlayState playState, BulletAppState bullet, Node root, HostedGameStatsService hostedGameStatsService) {
        this.playState = playState;
        this.bulletAppState = bullet;
        this.root = root;
        this.hostedGameStatsService = hostedGameStatsService;
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        Spatial nodeA = event.getNodeA();
        Spatial nodeB = event.getNodeB();
        if(!nodeA.getName().equals("Quad") && !nodeB.getName().equals("Quad")){   
            try{
                if(!triggeredTraps.contains(nodeB.getParent().getName()) && !triggeredTraps.contains(nodeA.getParent().getName())){            

                    if(isTrapCollision(nodeA, nodeB)){
                        triggerTrap(nodeA, nodeB);
                    } else if(isTrapCollision(nodeB, nodeA)){
                        triggerTrap(nodeB, nodeA);
                    } else if (event.getNodeA().getParent().getName().equals("playersNode") && event.getNodeB().getParent().getName().equals("playersNode")){                    
                        if(isMurder(nodeA, nodeB)){    
                            commitMurder(nodeA, nodeB);
                        } else if(isMurder(nodeB, nodeA)){
                            commitMurder(nodeB, nodeA);

                        }
                    }
                } 
            } catch(NullPointerException e){
                LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }
    }
    
    private boolean isTrapCollision(Spatial a, Spatial b){
        return (a.getParent().getName().equals("playersNode") && b.getParent().getParent().getName().equals("traps"));
        
    }
    
    private void triggerTrap(Spatial nodeA, Spatial nodeB){
        String trap = nodeB.getName();
        String[] list = trap.split(":");
        String owner = list[0];

        if(!nodeA.getName().equals(owner)){

            LOGGER.log(Level.INFO, "removing trap : " + nodeB.getParent().getName());
            hostedGameStatsService.triggeredTrap(nodeA.getName(), nodeB.getName());
            hostedGameStatsService.sendOutDeletedTraps();
            playState.deleteTrap(nodeA.getName(), nodeB.getName());

            triggeredTraps.add(nodeB.getParent().getName());
            root.detachChildNamed(nodeB.getParent().getName());                    
        }
    }
    
    private boolean isMurder(Spatial nodeA, Spatial nodeB){
        return (nodeA instanceof HumanNode && nodeB instanceof MonsterNode);
    }
    
    private void commitMurder(Spatial nodeA, Spatial nodeB){
        LOGGER.log(Level.INFO, nodeA.getName() + " is the victim \n" + nodeB.getName() + " is the killer");                        
        playState.playerGotKilled(nodeA.getName(), nodeB.getName());
        hostedGameStatsService.playerGotKilled(nodeA.getName(), nodeB.getName());
        hostedGameStatsService.sendOutKilled();  

    }
}
