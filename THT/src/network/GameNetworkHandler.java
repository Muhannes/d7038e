/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.network.Client;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiClientService;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcClientService;
import com.jme3.network.service.rpc.RpcHostedService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.handover.ClientHandoverService;
import network.services.gamesetup.HostedGameSetupService;
import network.services.login.HostedLoginService;
import network.util.NetConfig;
import static network.util.NetConfig.initSerializables;

/**
 *
 * @author ted
 */
public class GameNetworkHandler {
    
    private static final Logger LOGGER = Logger.getLogger(GameNetworkHandler.class.getName());
    
    private Server server;
    private Client lobbyClient;
    
    
    public GameNetworkHandler(){
        
        Logger.getLogger("").setLevel(Level.INFO);
        initSerializables();
        initGameServer();
        connectToLobbyServer();
        server.start();
        lobbyClient.start();
        // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
        NetConfig.networkDelay(150);
        getClientHandoverService().joinLobby();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initGameServer(){
        try {
            LOGGER.log(Level.INFO, "Starting server at port: {0}", Integer.toString(NetConfig.GAME_SERVER_PORT));
            // create and start the server
            server = Network.createServer(NetConfig.GAME_SERVER_PORT);
            //server.getServices().removeService(server.getServices().getService(ServerSerializerRegistrationsService.class));
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedGameSetupService());
            
            // Important to call this afer the server has been created!!!
            
            //server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            server.close();
        }
    }
    
    
    public void connectToLobbyServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_HANDOVER_SERVER_PORT});
            lobbyClient = Network.connectToServer(NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_HANDOVER_SERVER_PORT);
            lobbyClient.getServices().addService(new RpcClientService());
            lobbyClient.getServices().addService(new RmiClientService()); 
            lobbyClient.getServices().addService(new ClientHandoverService());
            System.out.println("services fetched");
            
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public synchronized void cleanUp(){
        if (lobbyClient != null && lobbyClient.isStarted()) {
            lobbyClient.close();
        }
    }
    
   public ClientHandoverService getClientHandoverService(){
       return lobbyClient.getServices().getService(ClientHandoverService.class);
   }
    
}
