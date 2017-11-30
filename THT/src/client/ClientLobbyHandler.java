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
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import networkutil.JoinRoomAckMessage;
import networkutil.LobbyRoomsMessage;

/**
 *
 * @author truls
 */
public class ClientLobbyHandler implements
        MessageListener<Client>,
        LobbyEmitter, 
        PlayerConnectionEmitter, 
        LobbySelectionListener{

    private PlayerConnectionListener playerConnectionListener;
    private LobbyListener lobbyListener;
    
    public ClientLobbyHandler(Client client){
        client.addMessageListener(this, JoinRoomAckMessage.class);
        client.addMessageListener(this, LobbyRoomsMessage.class);
    }
    
    @Override
    public void messageReceived(Client source, Message m) {
        if(m instanceof JoinRoomAckMessage){
    
        }else if(m instanceof LobbyRoomsMessage){
        
        }else{
        
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
    public void notifyLobbySelection(LobbyRoom lobbyRoom, int playerID) {
        
    }
    
}
