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
        
        // Our loggers, tune the level
        Logger.getLogger(LoginState.class.getName()).setLevel(Level.INFO);
        
        // Turning off Niftys verbose logging
        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
        
        clientNetworkManager = new ClientNetworkManager();
        
        clientNetworkManager.connectToServer();
        // Create and attach all states. TODO: move to function for cleaner code.
        
        LoginState loginScreen = new LoginState(clientNetworkManager.getClientLoginService());
        loginScreen.setEnabled(false);
        stateManager.attach(loginScreen);
        
        LobbyState lobbyState = new LobbyState(clientNetworkManager.getClientLobbyService());
        lobbyState.setEnabled(false);
        stateManager.attach(lobbyState);
        
        GameLobbyScreen gameLobbyScreen = new GameLobbyScreen(clientNetworkManager.getClientChatService(),
                clientNetworkManager.getClientLobbyService());
        gameLobbyScreen.setEnabled(false);
        stateManager.attach(gameLobbyScreen);
        
        SetupState setupState = new SetupState(clientNetworkManager.getClientGameSetupService(), 1);
        setupState.setEnabled(false);
        stateManager.attach(setupState);
        
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
    
    public static void main(String[] args){
       ClientApplication clientApplication = new ClientApplication();
       clientApplication.start();
    }

}
