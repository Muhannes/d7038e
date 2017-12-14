/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import api.models.LobbyRoom;
import api.models.Player;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import com.jme3.network.service.serializer.ServerSerializerRegistrationsService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.HostedChatService;
import network.services.handover.HostedHandoverService;
import network.services.lobby.HostedLobbyService;
import network.services.login.HostedLoginService;
import network.services.ping.HostedPingService;
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
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        LobbyNetworkHandler nh = new LobbyNetworkHandler();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LobbyNetworkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public LobbyNetworkHandler(){
        
        initSerializables();
        initPlayerServer();
        initHandoverServer();
        //startServers();
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
            playerServer.getServices().addService(new HostedLoginService());
            playerServer.getServices().addService(new HostedChatService());
            playerServer.getServices().addService(new HostedLobbyService());
            playerServer.getServices().addService(new HostedPingService());
            
            // Important to call this afer the playerServer has been created!!!
            
            //playerServer.start();
            
        } catch (IOException e) {
            e.printStackTrace();
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
            
            
        } catch (IOException e) {
            e.printStackTrace();
            handoverServer.close();
        }
    }
    
    public void startServers(){
        handoverServer.start();
        playerServer.start();
    }
}