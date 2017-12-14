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
import network.services.login.HostedLoginService;
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
    private Client gameClient;
    
    public ClientNetworkManager(){
        NetConfig.initSerializables();
    }
    
    public void connectToServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_PLAYER_SERVER_PORT});
            client = Network.connectToServer(NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_PLAYER_SERVER_PORT);
            client.getServices().addService(new RpcClientService());
            client.getServices().addService(new RmiClientService());
            client.getServices().addService(new ClientLoginService());
            client.getServices().addService(new ClientChatService());
            client.getServices().addService(new ClientLobbyService());
            client.getServices().addService(new ClientPingService());
            System.out.println("services fetched");
            
            // Not neded since server will send message to client with all serializables.
            //NetworkUtil.initSerializables();
            
            client.start();
            System.out.println("client Started");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void connectToGameServer(String ip, int port){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to game server at {0}:{1}", 
                    new Object[]{ip, port});
            // TODO: Change to use ip instead!
            gameClient = Network.connectToServer(ip, port);
            gameClient.getServices().addService(new RpcClientService());
            gameClient.getServices().addService(new RmiClientService());
            gameClient.getServices().addService(new ClientGameSetupService());
            System.out.println("services fetched");
            
            
            gameClient.start();
            // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
            NetConfig.networkDelay(50);
            System.out.println("client Started");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public synchronized void cleanUp(){
        if (client != null && client.isStarted()) {
            client.close();
        }
        if (gameClient != null && gameClient.isStarted()) {
            gameClient.close();
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
        return gameClient.getServices().getService(ClientGameSetupService.class);
    }
}

