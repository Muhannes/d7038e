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
import network.services.chat.HostedChatService;
import network.services.login.LoginListenerService;
import network.util.NetConfig;

/**
 *
 * @author hannes
 */
public class ChatNetworkHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ChatNetworkHandler.class.getName());
    private Client loginClient;
    private Server server;

    public ChatNetworkHandler() {
        NetConfig.initSerializables();
        initChatServer();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initChatServer(){
        try {
            LOGGER.log(Level.INFO, "Starting chat server at port: {0}", Integer.toString(NetConfig.CHAT_SERVER_PORT));
            // create and start the server
            server = Network.createServer(NetConfig.CHAT_SERVER_PORT);
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedChatService());
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            server.close();
        }
    }
    
    public void startServer(){
        server.start();
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
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        ChatNetworkHandler cnt = new ChatNetworkHandler();
        cnt.startServer();
        cnt.connectToLoginServer();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
