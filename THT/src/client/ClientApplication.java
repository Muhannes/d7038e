/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import client.network.ClientNetworkHandler;
import api.LobbyListener;
import api.LobbySelectionEmitter;
import api.LobbySelectionListener;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import api.models.Player;
import com.jme3.app.SimpleApplication;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author truls
 */
public class ClientApplication extends SimpleApplication implements 
        LobbyListener, 
        PlayerConnectionListener,
        LobbySelectionEmitter{
    
    private static final Logger LOGGER = Logger.getLogger(ClientApplication.class.getName());
    
    private LobbySelectionListener lobbySelectionListener;
    
    private ClientNetworkHandler clientNetworkHandler;

    @Override
    public void simpleInitApp() {
        Logger.getLogger("").setLevel(Level.FINE);
        
        LOGGER.log(Level.FINE, "simpleInitApp");
        
        /*clientNetworkHandler = new ClientNetworkHandler();
        
        clientNetworkHandler.connectToServer();
        
        clientNetworkHandler.getClientLobbyHandler().addLobbyListener(this);
        clientNetworkHandler.getClientLobbyHandler().addPlayerConnectionListener(this);
        
        addLobbySelectionListener(clientNetworkHandler.getClientLobbyHandler());*/
        //TODO Create GUI
        
        LobbyScreen lobbyScreen = new LobbyScreen();
        LoginScreen loginScreen = new LoginScreen(lobbyScreen);
        
        stateManager.attach(loginScreen);
        
        flyCam.setEnabled(false);
        setDisplayStatView(false);
    }

    @Override
    public void notifyLobby(LobbyRoom lobbyRoom) {
        LOGGER.log(Level.FINE, "notifyLobby: LobbyRoom = ", lobbyRoom);
        //TODO: Update GUI
    }
    
    @Override
    public void notifyPlayerConnection(Player player, LobbyRoom lobbyRoom) { 
        LOGGER.log(Level.FINE, "notifyPlayerConnection: Player = ", player);
    }

    @Override
    public void addLobbySelectionListener(LobbySelectionListener lobbySelectionListener) {
        this.lobbySelectionListener = lobbySelectionListener;
    }
    
    public static void main(String[] args){
       ClientApplication clientApplication = new ClientApplication();
       clientApplication.start();
    }

}
