/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby;

import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import java.io.IOException;
import networkutil.NetworkUtil;

/**
 *
 * @author hannes
 */
public class NetworkHandler {
    
    
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
            
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            HostedLobbyService hostedLobbyService = new HostedLobbyService();
            server.getServices().addService(hostedLobbyService);
            
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            server.close();
        }
        System.out.println("Server started");
    }
    
    
}
