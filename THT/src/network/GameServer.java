/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.movement.MovementSession;
import network.service.movement.PlayerMovement;

/**
 *
 * @author hannes
 */
public class GameServer extends SimpleApplication implements MovementSession {
    
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    private final List<PlayerMovement> movements = new ArrayList<>();
    
    private static MovementSession movementSession;
    
    private Node playersNode;
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        GameServer gameServer = new GameServer();
        GameNetworkHandler gnh = new GameNetworkHandler(movementSession);
        gnh.startServer();
        gnh.connectToLobbyServer();
        gnh.connectToLoginServer();
        gameServer.start(JmeContext.Type.Headless);
        
    }
    
    public static void sendOutMovements(){
        //Send out movements everything 10ms 
        LOGGER.log(Level.INFO, "Sending out to clients");                    
        
        new Runnable(){
            @Override
            public void run() {
                try {                    
                    Thread.sleep(100);                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                }
            }            
        }.run();
        
    }
    
    @Override
    public void simpleInitApp() {
        // Do intialization here.
        playersNode = (Node) this.rootNode.getChild("players");
    }

    @Override
    public void sendMessage(PlayerMovement playerMovement) {
        System.out.println("Receiving playermovement in GameServer");
        playersNode.getChild(playerMovement.id).getControl(CharacterControl.class).setWalkDirection(playerMovement.direction);
        playersNode.getChild(playerMovement.id).setLocalRotation(playerMovement.rotation);
        
        movements.add(playerMovement);
    }
    
}
