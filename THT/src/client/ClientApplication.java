/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.LostFocusBehavior;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.ClientNetworkManager;
import network.service.chat.client.ClientChatService;
import network.service.gamesetup.client.ClientGameSetupService;
import network.service.lobby.client.ClientLobbyService;
import network.service.login.client.ClientLoginService;
import network.service.movement.client.ClientMovementService;

/**
 *
 * @author truls
 */
public class ClientApplication extends SimpleApplication{
    
    private static final Logger LOGGER = Logger.getLogger(ClientApplication.class.getName());
    
    private ClientNetworkManager clientNetworkManager;
        
    @Override
    public void simpleInitApp() {
        // Default logger
        Logger.getLogger("").setLevel(Level.INFO);
        
        // Turning off Niftys verbose logging
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
        
        clientNetworkManager = new ClientNetworkManager();
        clientNetworkManager.connectToLoginServer();
        connectToLobbyServer();
        connectToChatServer();
        
        LoginState loginScreen = new LoginState();
        loginScreen.setEnabled(false);
        stateManager.attach(loginScreen);
        LobbyState lobbyState = new LobbyState();
        lobbyState.setEnabled(false);
        stateManager.attach(lobbyState);
        
        GameLobbyState gameLobbyScreen = new GameLobbyState();
        gameLobbyScreen.setEnabled(false);
        stateManager.attach(gameLobbyScreen);
        
        SetupState setupState = new SetupState();
        setupState.setEnabled(false);
        stateManager.attach(setupState);
        
        GameState gameState = new GameState();
        gameState.setEnabled(false);
        stateManager.attach(gameState);
        
        //Bullet physics for players, walls, objects
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(true);  
        stateManager.attach(bulletAppState);
        
        // Start app at login Screen
        loginScreen.setEnabled(true);
        
        flyCam.setEnabled(false);
        setDisplayStatView(false);
        
        setLostFocusBehavior(LostFocusBehavior.Disabled);
    }
    
    @Override
    public void destroy(){
        clientNetworkManager.cleanUp();
        // Clean up network resources
        
        super.destroy();
        // Clean up the rest
    }
    
    public void connectToGameServer(String ip, int port){
        clientNetworkManager.connectToGameServer(ip, port);
    }
    
    public void connectToLobbyServer(){
        clientNetworkManager.connectToLobbyServer();
    }
    
    public void connectToChatServer(){
        clientNetworkManager.connectToChatServer();
    }
    
    public ClientGameSetupService getGameSetupService(){
        return clientNetworkManager.getClientGameSetupService();
    }
    
    public ClientLoginService getClientLoginService(){
        return clientNetworkManager.getClientLoginService();
    }
    
    public ClientLobbyService getClientLobbyService(){
        return clientNetworkManager.getClientLobbyService();
    }
    
    public ClientChatService getClientChatService(){
        return clientNetworkManager.getClientChatService();
    }
    
    public ClientMovementService getClientMovementService(){
        return clientNetworkManager.getClientMovementService();
    }
    
    public static void main(String[] args){
       ClientApplication clientApplication = new ClientApplication();
       clientApplication.start();
    }

}
