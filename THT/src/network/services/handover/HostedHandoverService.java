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
import network.services.lobby.ClientLobbyListener;
import network.services.login.HostedLoginService;
import utils.eventbus.Event;
import utils.eventbus.EventBus;
import utils.eventbus.EventListener;

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
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(HostedLoginService.class.getName()).log(Level.SEVERE, null, ex);
        }
        RmiRegistry rmi = rmiHostedService.getRmiRegistry(connection);
        System.out.println("Sharing HandoverSession");
        rmi.share((byte)channel, gls, HandoverSession.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        LOGGER.log(Level.INFO, "Handover service stopped: Client id: {0}", connection.getId());
    }
    
    private HandoverSessionListener getCallback(HostedConnection connection){
        RmiRegistry rmiRegistry = rmiHostedService.getRmiRegistry(connection);
        HandoverSessionListener callback = rmiRegistry.getRemoteObject(HandoverSessionListener.class);
        if( callback == null){ 
            throw new RuntimeException("Unable to locate client callback for Handover service");
        }
        return callback;
    }

    @Override
    public void notifyEvent(Event event, Class<? extends Event> T) {
        if (T == SetupGameEvent.class) {
            System.out.println("Setup event occured hostedHandoverSErvice");
            SetupGameEvent setupGameEvent = (SetupGameEvent) event;
            if (!gameServers.isEmpty()) {
                GameServer gameServer = gameServers.remove(0);
                System.out.println("Sending setup to game server");
                gameServer.gameCallback.startSetup(setupGameEvent.getPlayers());
                for (ClientLobbyListener callback : setupGameEvent.getCallbacks()) {
                    System.out.println("Sending allready");
                    callback.allReady(gameServer.ipAddress, gameServer.port);
                }
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
            System.out.println("Ip received from gameServer: " + ip);
            gameServers.add(new GameServer(ip, port, getCallback(connection)));
            return true;
        }
        
    }

    
}
