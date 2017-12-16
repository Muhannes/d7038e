/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.handover;

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
import network.services.gamesetup.SetupGameEvent;
import network.util.NetConfig;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;
import network.services.lobby.LobbySessionListener;

/**
 *
 * @author hannes
 */
public class HostedHandoverService extends AbstractHostedConnectionService implements EventListener{
    
    private static final Logger LOGGER = Logger.getLogger(HostedHandoverService.class);
    private final List<GameServer> gameServers = new ArrayList<>();
    
    private RmiHostedService rmiHostedService;
    private int channel;
    
    public HostedHandoverService(){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
        EventBus.subscribe(this);
    }

    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiHostedService = getService(RmiHostedService.class);
        if( rmiHostedService == null ) {
            throw new RuntimeException("HandoverService requires an RMI service.");
        }    
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Handover service starter. Client id: {0}", connection.getId());
        HandoverSession gls = new GameLobbySessionImpl(connection);
        // Now we expose this object such that the client can get hold of it
        // DO NOT REMOVE SLEEP! I REPEAT, DO NOT REMOVE SLEEP!
        NetConfig.networkDelay(50);
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        rmi.share((byte)channel, gls, HandoverSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Handover service stopped: Client id: {0}", connection.getId());
    }
    
    private HandoverSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        return NetConfig.getCallback(rmiRegistry, HandoverSessionListener.class);
    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if (T == SetupGameEvent.class) {
            LOGGER.info("Setup event occured hostedHandoverSErvice");
            SetupGameEvent setupGameEvent = (SetupGameEvent) event;
            if (!gameServers.isEmpty()) {
                GameServer gameServer = gameServers.remove(0);
                LOGGER.info("Sending setup to game server");
                gameServer.gameCallback.startSetup(setupGameEvent.getPlayers());
                List<LobbySessionListener> callbacks = setupGameEvent.getCallbacks();
                for (LobbySessionListener callback : callbacks) {
                    callback.allReady(gameServer.ipAddress, gameServer.port);
                }
                LOGGER.info("All ready command sent out to clients");
            } else {
                LOGGER.severe("No GameServer Available!");
            }
        }
    }
    
    private class GameLobbySessionImpl implements HandoverSession {

        HostedConnection connection;

        public GameLobbySessionImpl(HostedConnection connection) {
            this.connection = connection;
        }
        
        
        
        @Override
        public boolean join(int key, int port) {
            String ip = connection.getAddress().split(":")[0];
            ip = ip.split("/")[1];
            gameServers.add(new GameServer(ip, port, getCallback(connection)));
            return true;
        }
        
    }

    
}
