/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.scene.Node;
import java.util.logging.Logger;
import network.service.movement.MovementSession;
import network.service.movement.PlayerMovement;
import network.service.movement.server.HostedMovementService;

/**
 *
 * @author ted
 */
public class PlayState extends BaseAppState implements MovementSession{

    private static final Logger LOGGER = Logger.getLogger(PlayState.class.getName());
    private GameServer app;
    private Node playersNode;
    private HostedMovementService hostedMovementService;

    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;
    }

    @Override
    protected void cleanup(Application app) {
        
    }

    @Override
    protected void onEnable() {
        LOGGER.info("Playstate enabled!");
        hostedMovementService = app.getHostedMovementService();
        playersNode = (Node) app.getRootNode().getChild("playersNode");
        if (playersNode == null) {
            LOGGER.severe("playersNode was null");
        }
        hostedMovementService.addListener(this);
        
    }

    @Override
    protected void onDisable() {
        hostedMovementService.removeListener(this);
    }

    @Override
    public void sendMessage(PlayerMovement playerMovement) {
        System.out.println("Receiving playermovement in GameServer");
        if (playersNode.getChild(playerMovement.id) == null) {
            LOGGER.severe("ID was wrong!");
        }else {
            app.enqueue(new Runnable() {
                @Override
                public void run() {
                    playersNode.getChild(playerMovement.id).setLocalTranslation(playerMovement.location);
                    playersNode.getChild(playerMovement.id).getControl(CharacterControl.class).setWalkDirection(playerMovement.direction);
                    //playersNode.getChild(playerMovement.id).setLocalRotation(playerMovement.rotation);
                    playersNode.getChild(playerMovement.id).getControl(CharacterControl.class).setViewDirection(playerMovement.rotation);
                    hostedMovementService.playerUpdated(playerMovement.id);
                }
            });
        }
                    
            
    }
    
}
