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
import network.services.gamelobbyservice.ClientGameLobbyService;
import network.services.gamesetup.HostedGameSetupService;
import network.services.lobby.HostedLobbyService;
import network.services.login.HostedLoginService;
import network.services.ping.HostedPingService;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class GameLobbyNetworkHandler {
    
    private static final Logger LOGGER = Logger.getLogger(GameLobbyNetworkHandler.class.getName());
    
    private Server server;
    private final int port = NetConfig.GAME_SERVER_PORT;
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        GameLobbyNetworkHandler glnh = new GameLobbyNetworkHandler();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameLobbyNetworkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public GameLobbyNetworkHandler(){
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
