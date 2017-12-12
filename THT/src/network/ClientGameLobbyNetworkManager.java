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
import network.services.gamelobbyservice.ClientGameLobbyService;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class ClientGameLobbyNetworkManager implements ClientStateListener {
    private static final Logger LOGGER = Logger.getLogger(ClientNetworkManager.class.getName());
    
    private Client client;
    
    public ClientGameLobbyNetworkManager(){}
    
    public void connectToServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.SERVER_NAME, NetConfig.SERVER_PORT});
            client = Network.connectToServer(NetConfig.SERVER_NAME, NetConfig.SERVER_PORT);
            client.getServices().addService(new RpcClientService());
            client.getServices().addService(new RmiClientService());            
            client.getServices().addService(new ClientGameLobbyService());
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
    
   public ClientGameLobbyService getClientGameLobbyService(){
       return client.getServices().getService(ClientGameLobbyService.class);
   }
}
