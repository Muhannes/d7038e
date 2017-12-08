/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import api.models.LobbyRoom;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.HostedChatService;
import network.services.gamesetup.HostedGameSetupService;
import network.services.lobby.HostedLobbyService;
import network.services.login.HostedLoginService;
import network.services.ping.HostedPingService;
import network.util.NetConfig;

/**
 *
 * @author hannes
 */
public class NetworkHandler {
    
    private static final Logger LOGGER = Logger.getLogger(NetworkHandler.class.getName());
    
    private Server server;
    private final int port = NetConfig.SERVER_PORT;
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        NetworkHandler nh = new NetworkHandler();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public NetworkHandler(){
        initServer();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initServer(){
        /*
        // Maximizing logger output
        Logger networkLog = Logger.getLogger("com.jme3.network"); 
        networkLog.setLevel(Level.FINEST);
        
        // Even more logs for debugging
        Logger rootLog = Logger.getLogger("");
        if( rootLog.getHandlers().length > 0 ) {
           rootLog.getHandlers()[0].setLevel(Level.FINEST);
        } 
        */
        
        Logger.getLogger("").setLevel(Level.INFO);
        
        try {
            LOGGER.log(Level.INFO, "Starting server at port: {0}", Integer.toString(port));
            // create and start the server
            server = Network.createServer(port);
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedLoginService());
            server.getServices().addService(new HostedChatService());
            server.getServices().addService(new HostedLobbyService());
            server.getServices().addService(new HostedPingService());
            server.getServices().addService(new HostedGameSetupService());
            
            // Important to call this afer the server has been created!!!
            initSerializables();
            
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            server.close();
        }
    }
    
    private static void initSerializables(){
        Serializer.registerClass(LobbyRoom.class);
    }
    
}
