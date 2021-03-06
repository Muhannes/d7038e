/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.util.logging.Logger;
import network.GameNetworkHandler;
import network.service.gamesetup.server.HostedGameSetupService;
import network.service.gamestats.server.HostedGameStatsService;
import network.service.movement.server.HostedMovementService;

/**
 * @author hannes
 */
public class GameServer extends SimpleApplication implements ConnectionListener{
    
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    public GameNetworkHandler gnh;
    
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        GameServer gameServer = new GameServer();
        
        gameServer.start(JmeContext.Type.Headless);
        
    }
    
    public GameServer(){
        
        gnh = new GameNetworkHandler();
    }
    
    @Override
    public void simpleInitApp() {
        // Do intialization here.
        
        gnh.startServer();
        gnh.addConnectionListener(this);
        
        
        WaitingState waitingState = new WaitingState();
        waitingState.setEnabled(false);
        this.stateManager.attach(waitingState);

        SetupState setupState = new SetupState();
        setupState.setEnabled(false);
        this.stateManager.attach(setupState);

        PlayState playState = new PlayState();
        playState.setEnabled(false);
        this.stateManager.attach(playState);
        
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);  
        stateManager.attach(bulletAppState);
        
        // Start app at login Screen
        waitingState.setEnabled(true);
        
    }
    
    public GameNetworkHandler getNetworkHandler(){
        return gnh;
    }
    
    public HostedGameSetupService getHostedGameSetupService(){
        return gnh.getHostedGameSetupService();
    }
    
    public HostedMovementService getHostedMovementService(){
        return gnh.getHostedMovementService();
    }
    
    public HostedGameStatsService getHostedGameStatsService(){
        return gnh.getHostedGameStatsService();
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        // conn added
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        if (!server.hasConnections()) {
            this.enqueue(new Runnable() {
                @Override
                public void run() {
                    stateManager.getState(SetupState.class).setEnabled(false);
                    stateManager.getState(PlayState.class).setEnabled(false);
                    stateManager.getState(WaitingState.class).setEnabled(true);
                }
            });
            
        }
    }
}
