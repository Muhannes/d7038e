/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.app.LostFocusBehavior;
import com.jme3.app.SimpleApplication;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.ClientNetworkManager;
import network.services.chat.client.ClientChatService;
import network.services.gamesetup.ClientGameSetupService;
import network.services.lobby.client.ClientLobbyService;
import network.services.login.ClientLoginService;

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
        
        GameLobbyScreen gameLobbyScreen = new GameLobbyScreen();
        gameLobbyScreen.setEnabled(false);
        stateManager.attach(gameLobbyScreen);
        
        SetupState setupState = new SetupState();
        setupState.setEnabled(false);
        stateManager.attach(setupState);
        
        GameState gameState = new GameState();
        gameState.setEnabled(false);
        stateManager.attach(gameState);
        
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
    
    public static void main(String[] args){
       ClientApplication clientApplication = new ClientApplication();
       clientApplication.start();
    }

}
