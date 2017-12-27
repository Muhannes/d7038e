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
import network.service.chat.client.ClientChatService;
import network.service.gamesetup.client.ClientGameSetupService;
import network.service.gamestats.client.ClientGameStatsService;
import network.service.lobby.client.ClientLobbyService;
import network.service.login.client.ClientLoginService;
import network.service.movement.client.ClientMovementService;
import network.service.ping.client.ClientPingService;
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
            LOGGER.log(Level.INFO, "Trying to connect to lobby server at {0}:{1}", 
                    new Object[]{NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_PLAYER_SERVER_PORT});
            lobbyClient = Network.connectToServer(NetConfig.LOBBY_SERVER_NAME, NetConfig.LOBBY_PLAYER_SERVER_PORT);
            lobbyClient.getServices().addService(new RpcClientService());
            lobbyClient.getServices().addService(new RmiClientService());
            lobbyClient.getServices().addService(new ClientLobbyService());
            lobbyClient.getServices().addService(new ClientPingService());
            
            lobbyClient.start();
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void connectToGameServer(String ip, int port){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to game server at {0}:{1}", 
                    new Object[]{ip, port});
            
            gameClient = Network.connectToServer(ip, port);
            gameClient.getServices().addService(new RpcClientService());
            gameClient.getServices().addService(new RmiClientService());
            gameClient.getServices().addService(new ClientGameSetupService());
            gameClient.getServices().addService(new ClientMovementService());
            gameClient.getServices().addService(new ClientGameStatsService());
            
            gameClient.start();
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void connectToLoginServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to login server at {0}:{1}", 
                    new Object[]{NetConfig.LOGIN_SERVER_NAME, NetConfig.LOGIN_SERVER_PORT});
            loginClient = Network.connectToServer(NetConfig.LOGIN_SERVER_NAME, NetConfig.LOGIN_SERVER_PORT);
            loginClient.getServices().addService(new RpcClientService());
            loginClient.getServices().addService(new RmiClientService()); 
            loginClient.getServices().addService(new ClientLoginService());
            
            loginClient.start();
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public void connectToChatServer(){
        try{
            LOGGER.log(Level.INFO, "Trying to connect to chat"
                    + "server at {0}:{1}", 
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
    
    public ClientMovementService getClientMovementService(){
        return gameClient.getServices().getService(ClientMovementService.class);
    }
  
    public ClientGameStatsService getClientGameStatsService(){
        return gameClient.getServices().getService(ClientGameStatsService.class);
    }
}

