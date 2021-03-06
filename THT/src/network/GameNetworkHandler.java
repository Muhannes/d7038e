/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import network.util.PortFinder;
import com.jme3.network.Client;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiClientService;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcClientService;
import com.jme3.network.service.rpc.RpcHostedService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.handover.ClientHandoverService;
import network.service.gamesetup.server.HostedGameSetupService;
import network.service.gamestats.server.HostedGameStatsService;
import network.service.movement.server.HostedMovementService;
import network.util.NetConfig;
import static network.util.NetConfig.initSerializables;

/**
 *
 * @author ted
 */
public class GameNetworkHandler {
    
    
    private static final Logger LOGGER = Logger.getLogger(GameNetworkHandler.class.getName());
    
    private Server server;
    private int serverPort = -1;
    private Client lobbyClient;
    private Client loginClient;
    
    public GameNetworkHandler(){
        
        Logger.getLogger("").setLevel(Level.INFO);
        initSerializables();
        initGameServer();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initGameServer(){
        try {
            serverPort = PortFinder.findFreePort();
            LOGGER.log(Level.INFO, "Starting server at port: {0}", Integer.toString(serverPort));
            // create and start the server
            server = Network.createServer(serverPort);
            
            //server.getServices().removeService(server.getServices().getService(ServerSerializerRegistrationsService.class));
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedGameSetupService());
            server.getServices().addService(new HostedMovementService());
            server.getServices().addService(new HostedGameStatsService());
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            server.close();
        }
    }
    
    public int getServerPort(){
        return serverPort;
    }
    
    public void addConnectionListener(ConnectionListener connectionListener){
        server.addConnectionListener(connectionListener);
    }
    
    
    public void connectToLobbyServer(){
        if (serverPort < 1) {
            LOGGER.severe("No Server port was set. this must be done before trying to connect to lobby");
            return;
        }
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_HANDOVER_SERVER_PORT});
            lobbyClient = Network.connectToServer(NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_HANDOVER_SERVER_PORT);
            lobbyClient.getServices().addService(new RpcClientService());
            lobbyClient.getServices().addService(new RmiClientService()); 
            lobbyClient.getServices().addService(new ClientHandoverService(serverPort));
            
            lobbyClient.start();
            getClientHandoverService().joinLobby();
        }catch(IOException ex){
               LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void disconnectFromLobbyServer(){
        if (lobbyClient != null && lobbyClient.isStarted()) {
            lobbyClient.close();
        }
    }
    
    public synchronized void cleanUp(){
        disconnectFromLobbyServer();
    }
    
   public ClientHandoverService getClientHandoverService(){
       return lobbyClient.getServices().getService(ClientHandoverService.class);
   }
   
   public HostedMovementService getHostedMovementService(){
       return server.getServices().getService(HostedMovementService.class);
   }
   
   public HostedGameStatsService getHostedGameStatsService(){
       return server.getServices().getService(HostedGameStatsService.class);
   }
   
   public HostedGameSetupService getHostedGameSetupService(){
       return server.getServices().getService(HostedGameSetupService.class);
   }
   
   public void startServer(){
       server.start();
   }
   
   
    
}
