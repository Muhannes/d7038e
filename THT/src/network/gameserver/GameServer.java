/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.GameNetworkHandler;
import network.service.gamesetup.server.HostedGameSetupService;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionListener;
import network.service.movement.PlayerMovement;
import network.service.movement.server.HostedMovementService;

/**
 * @author hannes
 */
public class GameServer extends SimpleApplication{
    
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    private GameNetworkHandler gnh;
    
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        GameServer gameServer = new GameServer();
        
        gameServer.start(JmeContext.Type.Headless);
        
    }
    
    @Override
    public void simpleInitApp() {
        // Do intialization here.
        gnh = new GameNetworkHandler();
        
        gnh.startServer();
        gnh.connectToLobbyServer();
        gnh.connectToLoginServer();
        
        
        PlayState playState = new PlayState();
        playState.setEnabled(false);
        this.stateManager.attach(playState);
        SetupState setupState = new SetupState();
        setupState.setEnabled(false);
        this.stateManager.attach(setupState);
        WaitingState waitingState = new WaitingState();
        waitingState.setEnabled(false);
        this.stateManager.attach(waitingState);
        
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);  
        stateManager.attach(bulletAppState);
        
        // Start app at login Screen
        waitingState.setEnabled(true);
        
    }
    
    public HostedGameSetupService getHostedGameSetupService(){
        return gnh.getHostedGameSetupService();
    }
    
    public void ready(){
        this.enqueue(() -> {
            this.getStateManager().getState(SetupState.class).setEnabled(false);
            this.getStateManager().getState(PlayState.class).setEnabled(true);
        });
    }
}
