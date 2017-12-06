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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author truls
 */
public class ClientPingService extends AbstractClientService{

    Logger LOGGER = Logger.getLogger(ClientPingService.class.getName());
    
    private List<PingListener> listeners = new ArrayList<>();
    
    private PingCallback callback;
    
    private Pinger delegate;
    
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
        rmiService.share((byte)channel, callback, PingListener.class);
    }
    
    public Pinger getDelegate(){
        if(delegate == null){
            delegate = rmiService.getRemoteObject(Pinger.class);
            if(delegate == null){
                throw new RuntimeException("No remote Pinger object found");
            }
        }
        return delegate;
    }
    
    void addPingListener(PingListener listener){
        listeners.add(listener);
    }
    
    private class PingCallback implements PingListener{
        
        @Override
        public void notifyPing(int ms) {
            LOGGER.log(Level.FINE, "Ping {0} ms", ms);
            getDelegate().reply();
            for(PingListener l : listeners){
                l.notifyPing(ms);
            }
        }
    
    }
    
}
