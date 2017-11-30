/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby.network;

import api.LobbyListener;
import api.LobbySelectionEmitter;
import api.LobbySelectionListener;
import api.PlayerConnectionListener;
import api.models.LobbyRoom;
import api.models.Player;
import com.jme3.network.Network;
import com.jme3.network.Server;
import java.io.IOException;
import networkutil.NetworkUtil;

/**
 *
 * @author hannes
 */
public class NetworkHandler implements LobbyListener, LobbySelectionEmitter, PlayerConnectionListener{
    
    
    private Server server;
    private final int port = NetworkUtil.LOBBY_SERVER_PORT;
    
    public NetworkHandler(){
        NetworkUtil.initSerializables();
        initServer();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initServer(){
        try {
            System.out.println("Using port " + port);
            // create and start the server
            server = Network.createServer(port);
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            server.close();
        }
        System.out.println("Server started");
        
        // add a listener that reacts on incoming network packets
        server.addMessageListener(new LobbyMessageListener()); //TODO: Add messagetypes here.
        System.out.println("ServerListener activated and added to server");
    }

    @Override
    public void notifyLobby(LobbyRoom lobbyRoom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addLobbySelectionListener(LobbySelectionListener lobbySelectionListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyPlayerConnection(Player player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
