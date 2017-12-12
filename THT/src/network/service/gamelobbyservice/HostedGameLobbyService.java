/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.gamelobbyservice;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import network.services.chat.ChatSession;
import network.services.chat.ChatSessionImpl;
import network.services.chat.ChatSessionListener;
import network.services.chat.ChatSpace;
import network.services.chat.HostedChatService;
import network.services.gamesetup.SetupGameEvent;
import network.services.lobby.ClientLobbyListener;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

/**
 *
 * @author hannes
 */
public class HostedGameLobbyService extends AbstractHostedConnectionService implements EventListener{
    
    private static final Logger LOGGER = Logger.getLogger(HostedGameLobbyService.class);
    private final List<GameServer> gameServers = new ArrayList<>();
    
    private RmiHostedService rmiHostedService;
    private int channel;
    
    public HostedGameLobbyService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("ChatHostedService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service started. Client id: {0}", connection.getId());
        GameLobbySession gls = new GameLobbySessionImpl(connection);
        // Now we expose this object such that the client can get hold of it
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, gls, GameLobbySession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Chat service stopped: Client id: {0}", connection.getId());
    }
    
    private GameLobbySessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        GameLobbySessionListener callback = rmiRegistry.getRemoteObject(GameLobbySessionListener.class);
        if( callback == null){ 
            throw new RuntimeException("Unable to locate client callback for GameService");
        }
        return callback;
    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if (T == SetupGameEvent.class) {
            SetupGameEvent setupGameEvent = (SetupGameEvent) event;
            if (!gameServers.isEmpty()) {
                GameServer gameServer = gameServers.remove(0);
                gameServer.gameCallback.startSetup(setupGameEvent.getPlayers());
                for (ClientLobbyListener callback : setupGameEvent.getCallbacks()) {
                    callback.allReady(gameServer.ipAddress, gameServer.port);
                }
            } else {
                LOGGER.severe("No GameServer Available!");
            }
        }
    }
    
    private class GameLobbySessionImpl implements GameLobbySession {

        HostedConnection connection;

        public GameLobbySessionImpl(HostedConnection connection) {
            this.connection = connection;
        }
        
        
        
        @Override
        public boolean join(int key, int port) {
            String ip = connection.getAddress().split(":")[0];
            gameServers.add(new GameServer(ip, port, getCallback(connection)));
            return true;
        }
        
    }

    
}
