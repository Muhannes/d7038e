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
        if(!event.getNodeA().getName().equals("Quad") && !event.getNodeB().getName().equals("Quad")){   

            /*if(!triggeredTraps.contains(event.getNodeB().getParent().getName()) && !triggeredTraps.contains(event.getNodeA().getParent().getName())){            
 
                if(event.getNodeA().getParent().getName().equals("playersNode") && event.getNodeB().getParent().getParent().getName().equals("traps")){
                   
                    String trap = event.getNodeB().getName();
                    String[] list = trap.split(":");
                    String owner = list[0];
                   
                    if(!event.getNodeA().getName().equals(owner)){

                        LOGGER.log(Level.INFO, "removing trap : " + event.getNodeB().getParent().getName());
                        hostedGameStatsService.triggeredTrap(event.getNodeA().getName(), event.getNodeB().getName());
                        hostedGameStatsService.sendOutDeletedTraps();
                        playState.deleteTrap(event.getNodeA().getName(), event.getNodeB().getName());

                        triggeredTraps.add(event.getNodeB().getParent().getName());
                        root.detachChildNamed(event.getNodeB().getParent().getName());                    
                    }
                } else if(event.getNodeB().getParent().getName().equals("playersNode") && event.getNodeA().getParent().getParent().getName().equals("traps")){

                    String trap = event.getNodeB().getName();
                    String[] list = trap.split(":");
                    String owner = list[0];
                   
                    if(!event.getNodeB().getName().equals(owner)){

                        LOGGER.log(Level.INFO, "removing trap : " + event.getNodeA().getParent().getName());
                        hostedGameStatsService.triggeredTrap(event.getNodeB().getName(), event.getNodeA().getName());
                        hostedGameStatsService.sendOutDeletedTraps();
                        playState.deleteTrap(event.getNodeB().getName(), event.getNodeA().getName());

                        triggeredTraps.add(event.getNodeB().getParent().getName());
                        root.detachChildNamed(event.getNodeB().getParent().getName());                    
                    }
                }
            } 
*/
            //LOGGER.log(Level.INFO, "Node A : " + event.getNodeA().getName() + " - " + event.getNodeA().getParent().getName() + " - " + event.getNodeA().getParent().getParent().getName() + " - " + event.getNodeA().getParent().getParent().getParent().getName());
            //LOGGER.log(Level.INFO, "Node B : " + event.getNodeB().getName() + " - " + event.getNodeB().getParent().getName() + " - " + event.getNodeB().getParent().getParent().getName() + " - " + event.getNodeB().getParent().getParent().getParent().getName());
                
//            if(event.getNodeA().getParent().getName().equals("playersNode") && event.getNodeB().getParent().getName().equals("playersNode")){
            if(event.getNodeA().getParent().getName().equals("playersNode") && event.getNodeB().getParent().getParent().getName().equals("traps")){
                //Cant check entity type here, send collision to playState and hostedGameStatsService!
                
                LOGGER.log(Level.INFO, "Collision between" + event.getNodeA().getName() + " and " + event.getNodeB().getName());
                playState.playerGotKilled(event.getNodeA().getName(), event.getNodeB().getName());
                
                hostedGameStatsService.playerGotKilled(event.getNodeA().getName(), event.getNodeA().getName());
                hostedGameStatsService.sendOutKilled();
            }
        }
    }
}
