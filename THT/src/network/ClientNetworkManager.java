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
import network.services.chat.client.ClientChatService;
import network.services.gamesetup.ClientGameSetupService;
import network.services.lobby.client.ClientLobbyService;
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
    
    private Client lobbyClient;
    private Client gameClient;
    private Client loginClient;
    private Client chatClient;
    
    public ClientNetworkManager(){
        NetConfig.initSerializables();
    }
    
    public void connectToLobbyServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_PLAYER_SERVER_PORT});
            lobbyClient = Network.connectToServer(NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_PLAYER_SERVER_PORT);
            lobbyClient.getServices().addService(new RpcClientService());
            lobbyClient.getServices().addService(new RmiClientService());
            //lobbyClient.getServices().addService(new ClientChatService());
            lobbyClient.getServices().addService(new ClientLobbyService());
            lobbyClient.getServices().addService(new ClientPingService());
            System.out.println("services fetched");
            
            // Not neded since server will send message to lobbyClient with all serializables.
            //NetworkUtil.initSerializables();
            
            lobbyClient.start();
            System.out.println("client Started");
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
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
            System.out.println("client Started");
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void connectToLoginServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.LOGIN_SERVER_NAME, NetConfig.LOGIN_SERVER_PORT});
            loginClient = Network.connectToServer(NetConfig.LOGIN_SERVER_NAME, NetConfig.LOGIN_SERVER_PORT);
            loginClient.getServices().addService(new RpcClientService());
            loginClient.getServices().addService(new RmiClientService()); 
            loginClient.getServices().addService(new ClientLoginService());
            System.out.println("services fetched");
            
            loginClient.start();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void connectToChatServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to server at {0}:{1}", 
                    new Object[]{NetConfig.CHAT_SERVER_NAME, NetConfig.CHAT_SERVER_PORT});
            chatClient = Network.connectToServer(NetConfig.CHAT_SERVER_NAME, NetConfig.CHAT_SERVER_PORT);
            chatClient.getServices().addService(new RpcClientService());
            chatClient.getServices().addService(new RmiClientService()); 
            chatClient.getServices().addService(new ClientChatService());
            
            chatClient.start();
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * TODO: Clean all!
     */
    public synchronized void cleanUp(){
        if (lobbyClient != null && lobbyClient.isStarted()) {
            lobbyClient.close();
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
        return loginClient.getServices().getService(ClientLoginService.class);
    }
    
    public ClientChatService getClientChatService(){
        return chatClient.getServices().getService(ClientChatService.class);
    }
    
    public ClientLobbyService getClientLobbyService(){
        return lobbyClient.getServices().getService(ClientLobbyService.class);
    }
    
    public ClientGameSetupService getClientGameSetupService(){
        return gameClient.getServices().getService(ClientGameSetupService.class);
    }
    
    /**
     * TODO: remove? this exists in login client service
     * @return 
     */
    public int getGlobalId(){
        return 0; //client.getId();
    }
}

