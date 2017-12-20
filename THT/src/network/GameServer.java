/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.service.movement.server.HostedMovementService;

/**
 * @author hannes
 */
public class GameServer extends SimpleApplication{
    
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    private final List<PlayerMovement> movements = new ArrayList<>();
    
    
    private HostedMovementService hostedMovementService;
    
    private static MovementSession movementSession;
    private GameNetworkHandler gnh;
    
    private Node playersNode;
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        GameServer gameServer = new GameServer();
        
        gameServer.start(JmeContext.Type.Headless);
        
        
    }
    
    
    @Override
    public void simpleInitApp() {
        // Do intialization here.
        playersNode = new Node("players");
        this.rootNode.attachChild(playersNode);
        
        gnh = new GameNetworkHandler();
        
        gnh.startServer();
        gnh.connectToLobbyServer();
        gnh.connectToLoginServer();
        
        gnh.getHostedMovementService().setPlayersNode(playersNode);
        

    }

    
}
