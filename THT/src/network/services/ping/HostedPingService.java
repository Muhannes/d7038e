/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.ping;

import com.jme3.network.HostedConnection;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractHostedConnectionService;
import com.jme3.network.service.HostedServiceManager;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import com.sun.istack.internal.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 *
 * @author truls
 */
public class HostedPingService extends AbstractHostedConnectionService{
    
    Logger LOGGER = Logger.getLogger(HostedPingService.class);
    
    private static final String PING = "ping";
    
    private RmiHostedService rmiService;
    // Used to sync with client and send data
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public HostedPingService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public HostedPingService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(HostedServiceManager serviceManager) {
        rmiService = getService(RmiHostedService.class);
        if(rmiService == null) {
            throw new RuntimeException("HostedPingService requires an RMI service.");
        } 
    }
    
    @Override
    public void startHostingOnConnection(HostedConnection connection) {        
        // The newly connected client will be represented by this object on
        // the server side
        PingerImpl session = new PingerImpl(connection);
        connection.setAttribute(PING, session);
        
        // Share the session as an RMI resource to the client
        RmiRegistry rmi = rmiService.getRmiRegistry(connection);
        if (rmi == null) {
            throw new Error("RMI is null!");
        }
        
        rmi.share((byte)channel, session, Pinger.class);
    }

    @Override
    public void stopHostingOnConnection(HostedConnection connection) {
        PingerImpl p = connection.getAttribute(PING);
        p.cleanup();
        connection.setAttribute(PING, null);
    }
    
    private class PingerImpl implements Pinger{

        private HostedConnection connection;
        private PingListener callback;
        
        private ScheduledExecutorService executor;
        
        private long RTT;
        private long oldTime;
        
        public PingerImpl(HostedConnection connection) {
            this.connection = connection;
            RTT = 0;
            executor = Executors.newScheduledThreadPool(1);
            
            Runnable task = () -> {
                ping();
            };
            
            executor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        }
        
        private void ping(){
            oldTime = System.currentTimeMillis();
            getCallback().notifyPing((int)RTT);
        }

        @Override
        public void reply() {
            RTT = System.currentTimeMillis() - oldTime;
        }
        
         private PingListener getCallback(){
            if(callback == null){
                RmiRegistry rmi = rmiService.getRmiRegistry(connection);
                callback = rmi.getRemoteObject(PingListener.class);
                if(callback == null){
                    throw new RuntimeException("No client callback found for LoginSessionListener");
                }
            }
            return callback;
        }
         
        void cleanup(){
            executor.shutdownNow();
        }
        
    }
    
}
