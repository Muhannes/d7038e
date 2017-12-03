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
import api.PlayerReadyEmitter;
import api.PlayerReadyListener;
import api.models.LobbyRoom;
import api.models.Player;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Network;
import com.jme3.network.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import networkutil.JoinRoomAckMessage;
import networkutil.JoinRoomMessage;
import networkutil.LeaveRoomMessage;
import networkutil.LobbyRoomsMessage;
import networkutil.NetworkUtil;

/**
 *
 * @author hannes
 */
public class NetworkHandler implements LobbyListener, PlayerConnectionListener{
    
    
    private Server server;
    private final int port = NetworkUtil.LOBBY_SERVER_PORT;
    
    private LobbyMessageListener lobbyMessageListener;
    
    public NetworkHandler(LobbyMessageListener msgListener){
        this.lobbyMessageListener = msgListener;
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
        //lobbyMessageListener = new LobbyMessageListener();
        // add a listener that reacts on incoming network packets
        server.addMessageListener(lobbyMessageListener, JoinRoomMessage.class, LeaveRoomMessage.class); //TODO: Add messagetypes here.
        System.out.println("ServerListener activated and added to server");
    }
    
    public void sendJoinRoomAckMessage(boolean ok, int playerID, int roomID){
        JoinRoomAckMessage message = new JoinRoomAckMessage(ok);
        HostedConnection conn = server.getConnection(playerID);
        if (ok) {
            conn.setAttribute(LobbyNetworkStates.ROOM_ID, roomID);
            server.broadcast(Filters.equalTo(conn), message);
        } else {
            server.broadcast(Filters.equalTo(conn), message);
        }
    }
    
    public void sendLobbyRoomsMessage(List<LobbyRoom> lobbyRooms, List<HostedConnection> clients){
        LobbyRoomsMessage message = new LobbyRoomsMessage(lobbyRooms);
        server.broadcast(Filters.in(clients), message);
    }

    @Override
    public void notifyLobby(LobbyRoom lobbyRoom) {
        List<LobbyRoom> rooms = new ArrayList<>();
        rooms.add(lobbyRoom);
        // Get all clients not in a lobbyRoom
        Predicate<HostedConnection> predicate = p -> (int) p.getAttribute(LobbyNetworkStates.ROOM_ID) != -1;
        List<HostedConnection> clients = getFilteredHosts(predicate);
        
        sendLobbyRoomsMessage(rooms, clients);
    }

    @Override
    public void notifyPlayerConnection(Player player, LobbyRoom lobbyRoom) {
        // TODO: Make message to send to all players in lobbyRoom notifying that player has joined.
    }
    
    public void addConnectionListener(ConnectionListener cl){
        server.addConnectionListener(cl);
    }
    
    private List<HostedConnection> getFilteredHosts(Predicate p){
        List<HostedConnection> hosts = new ArrayList();
        Collection connections = server.getConnections();
        hosts.addAll(connections);
        hosts.removeIf(p);
        return hosts;
    }
    
}
