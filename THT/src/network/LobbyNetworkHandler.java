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
import network.services.handover.HostedHandoverService;
import network.services.lobby.server.HostedLobbyService;
import network.services.login.LoginListenerService;
import network.services.ping.server.HostedPingService;
import network.util.NetConfig;
import static network.util.NetConfig.initSerializables;

/**
 *
 * @author hannes
 */
public class LobbyNetworkHandler {
    
    private static final Logger LOGGER = Logger.getLogger(LobbyNetworkHandler.class.getName());
    
    private Server playerServer;
    private Server handoverServer;
    
    private Client loginClient;
    
    public LobbyNetworkHandler(){
        
        initSerializables();
        initPlayerServer();
        initHandoverServer();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initPlayerServer(){
        Logger.getLogger("").setLevel(Level.INFO);
        
        try {
            LOGGER.log(Level.INFO, "Starting server at port: {0}", Integer.toString(NetConfig.LOBBY_PLAYER_SERVER_PORT));
            // create and start the playerServer
            playerServer = Network.createServer(NetConfig.LOBBY_PLAYER_SERVER_PORT);
            playerServer.getServices().addService(new RpcHostedService());
            playerServer.getServices().addService(new RmiHostedService());
            //playerServer.getServices().addService(new HostedChatService());
            playerServer.getServices().addService(new HostedLobbyService());
            playerServer.getServices().addService(new HostedPingService());
            
            // Important to call this afer the playerServer has been created!!!
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            playerServer.close();
        }
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initHandoverServer(){
        Logger.getLogger("").setLevel(Level.INFO);
        
        try {
            LOGGER.log(Level.INFO, "Starting server at port: {0}", Integer.toString(NetConfig.LOBBY_HANDOVER_SERVER_PORT));
            // create and start the playerServer
            handoverServer = Network.createServer(NetConfig.LOBBY_HANDOVER_SERVER_PORT);
            
            handoverServer.getServices().addService(new RpcHostedService());
            handoverServer.getServices().addService(new RmiHostedService());
            handoverServer.getServices().addService(new HostedHandoverService());
            
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            handoverServer.close();
        }
    }
    
    public void connectToLoginServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.LOGIN_SERVER_NAME, NetConfig.LOGIN_SERVER_PORT});
            loginClient = Network.connectToServer(NetConfig.LOGIN_SERVER_NAME, NetConfig.LOGIN_SERVER_PORT);
            loginClient.getServices().addService(new RpcClientService());
            loginClient.getServices().addService(new RmiClientService()); 
            loginClient.getServices().addService(new LoginListenerService());
            
            loginClient.start();
            loginClient.getServices().getService(LoginListenerService.class).listenForLogins();
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void startServers(){
        handoverServer.start();
        playerServer.start();
    }
    
}
