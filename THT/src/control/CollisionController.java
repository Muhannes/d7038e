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
public class CollisionController implements PhysicsCollisionListener{

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
    
    public void shutDown(){
        bulletAppState.getPhysicsSpace().removeCollisionListener(this);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        Spatial nodeA = event.getNodeA();
        Spatial nodeB = event.getNodeB();
        if(!nodeA.getName().equals("Quad") && !nodeB.getName().equals("Quad")){
            if(nodeA.getParent() == null || nodeB.getParent() == null){
                return;
            }
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
                    } else if(caughtMonkey(nodeA, nodeB)){
                        gotHim(nodeA, nodeB);
                    } else if(caughtMonkey(nodeB, nodeA)){
                        gotHim(nodeB, nodeA);
                    } else {}
                }
            } 
            
        }
    }
    
    private boolean isTrapCollision(Spatial a, Spatial b){
        if (b.getParent().getParent() != null) {
            return (a.getParent().getName().equals("playersNode") && b.getParent().getParent().getName().equals("traps"));
        } else {
            return false;
        }
        
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
        if(!playState.allDead()){
            playState.playerGotKilled(nodeA.getName(), nodeB.getName());
            hostedGameStatsService.playerGotKilled(nodeA.getName(), nodeB.getName());
            hostedGameStatsService.sendOutKilled();  
        } else {
            hostedGameStatsService.gameover();
            playState.gameover();
        }
    }
    
    
    private boolean caughtMonkey(Spatial nodeA, Spatial nodeB){
        return (nodeA instanceof HumanNode && nodeB instanceof MonkeyNode);
    }
    
    private void gotHim(Spatial nodeA, Spatial nodeB){
        LOGGER.log(Level.INFO, nodeA.getName() + " caught " + nodeB.getName());
        if(!playState.allCaught()){
            playState.monkeyGotCaught(nodeB.getName());
            hostedGameStatsService.playerCaughtMonkey(nodeA.getName(), nodeB.getName());
            hostedGameStatsService.sendOutMonkeyInfo();  
        } else {
            hostedGameStatsService.gameover();
            playState.gameover();
        }
    }    
}
