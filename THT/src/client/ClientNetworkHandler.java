/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import api.LobbyEmitter;
import api.LobbyListener;
import api.LobbySelectionListener;
import api.PlayerConnectionEmitter;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author truls
 */
public class ClientNetworkHandler implements 
        MessageListener<Client>,
        ClientStateListener,
        LobbyEmitter, 
        PlayerConnectionEmitter, 
        LobbySelectionListener{
    
    private static final Logger LOGGER = Logger.getLogger(ClientNetworkHandler.class.getName());
    
    private PlayerConnectionListener playerConnectionListener;
    private LobbyListener lobbyListener;
    
    private Client client;
    
    public ClientNetworkHandler(){
       
    }
    
    void connectToServer(){
        try{
            LOGGER.log(Level.FINE, "Trying to connect to server");
            client = Network.connectToServer("localhost", 7020);
            client.addClientStateListener(this);
            client.start();          
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void addLobbyListener(LobbyListener lobbyListener) {
        this.lobbyListener = lobbyListener;
    }

    @Override
    public void addPlayerConnectionListener(PlayerConnectionListener playerConnectionListener) {
        this.playerConnectionListener = playerConnectionListener;
    }

    @Override
    public void notifyLobbySelection(LobbyRoom lobbyRoom) {
        
    }

    @Override
    public void messageReceived(Client source, Message m) {
        LOGGER.log(Level.FINE, "Message received: source = ", source.toString());
    }

    @Override
    public void clientConnected(Client c) {
        LOGGER.log(Level.FINE, "Connected to server");   
    }

    @Override
    public void clientDisconnected(Client c, DisconnectInfo info) {
        LOGGER.log(Level.FINE, "Disconnected from server.\nReason: ", info.reason);  
    }
    
}
