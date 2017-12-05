/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.HostedChatService;
import network.services.lobby.HostedLobbyService;
import network.services.login.HostedLoginService;
import network.util.NetConfig;
import networkutil.NetworkUtil;

/**
 *
 * @author hannes
 */
public class NetworkHandler {
    
    
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
        
        try {
            System.out.println("Using port " + port);
            // create and start the server
            server = Network.createServer(port);
            
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedLoginService());
            server.getServices().addService(new HostedChatService());
            server.getServices().addService(new HostedLobbyService());
            
            // Important to call this afer the server has been created!!!
            NetworkUtil.initSerializables();
            
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            server.close();
        }
        System.out.println("Server started");
    }
    
    
}
