/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Network;
import com.jme3.network.service.rmi.RmiClientService;
import com.jme3.network.service.rpc.RpcClientService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.chat.ClientChatService;
import network.services.gamesetup.ClientGameSetupService;
import network.services.lobby.ClientLobbyService;
import network.services.login.ClientLoginService;
import network.services.ping.ClientPingService;
import network.util.NetConfig;

/**
 * Master class of network related stuff.
 * @author truls
 */
public class ClientNetworkManager implements 
        ClientStateListener {
    
    private static final Logger LOGGER = Logger.getLogger(ClientNetworkManager.class.getName());
    
    private Client client;
    
    public ClientNetworkManager(){}
    
    public void connectToServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.SERVER_NAME, NetConfig.SERVER_PORT});
            client = Network.connectToServer(NetConfig.SERVER_NAME, NetConfig.SERVER_PORT);
            client.getServices().addService(new RpcClientService());
            client.getServices().addService(new RmiClientService());
            client.getServices().addService(new ClientLoginService());
            client.getServices().addService(new ClientChatService());
            client.getServices().addService(new ClientLobbyService());
            client.getServices().addService(new ClientPingService());
            client.getServices().addService(new ClientGameSetupService());
            System.out.println("services fetched");
            
            // Not neded since server will send message to client with all serializables.
            //NetworkUtil.initSerializables();
            
            client.start();
            System.out.println("client Started");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public synchronized void cleanUp(){
        if (client != null && client.isStarted()) {
            client.close();
        }
    }

    @Override
    public void clientConnected(Client c) {
        LOGGER.log(Level.INFO, "Connected to server");  
    }

    @Override
    public void clientDisconnected(Client c, ClientStateListener.DisconnectInfo info) {
        LOGGER.log(Level.INFO, "Disconnected from server.\nReason: {0}", info.reason);  
    }
    
    public ClientLoginService getClientLoginService(){
        return client.getServices().getService(ClientLoginService.class);
    }
    
    public ClientChatService getClientChatService(){
        return client.getServices().getService(ClientChatService.class);
    }
    
    public ClientLobbyService getClientLobbyService(){
        return client.getServices().getService(ClientLobbyService.class);
    }
    
    public ClientGameSetupService getClientGameSetupService(){
        return client.getServices().getService(ClientGameSetupService.class);
    }
}

