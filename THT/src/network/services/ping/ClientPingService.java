/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.ping;

import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author truls
 */
public class ClientPingService extends AbstractClientService{

    private static final Logger LOGGER = Logger.getLogger(ClientPingService.class);
    
    private List<PingSessionListener> listeners = new ArrayList<>();
    
    private PingCallback callback;
    // Server calls this object
    
    private PingSession delegate;
    // Client calls this object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    
    public ClientPingService(){
        this(MessageConnection.CHANNEL_DEFAULT_RELIABLE);
    }
    
    public ClientPingService(int channel){
        this.channel = channel;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if(rmiService == null){
            throw new RuntimeException("ClientLoginService requires RmiService");
        }
        
        callback = new PingCallback();
        
        // Share the callback with the server
        rmiService.share((byte)channel, callback, PingSessionListener.class);
    }
    
    public PingSession getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(PingSession.class);
            if(delegate == null){
                throw new RuntimeException("No remote Pinger object found");
            }
        }
        return delegate;
    }
    
    void addPingListener(PingSessionListener listener){
        listeners.add(listener);
    }
    
    private class PingCallback implements PingSessionListener{
        
        @Override
        public void notifyPing(int ms) {
            LOGGER.log(Level.FINEST, "Ping {0} ms", ms);
            getDelegate().reply();
            listeners.forEach(l -> l.notifyPing(ms));
        }
    
    }
    
}
