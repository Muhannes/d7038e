/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.handover;

import network.service.gamesetup.PlayerInfoEvent;
import com.jme3.network.Client;
import com.jme3.network.MessageConnection;
import com.jme3.network.service.AbstractClientService;
import com.jme3.network.service.ClientServiceManager;
import com.jme3.network.service.rmi.RmiClientService;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.login.Account;
import network.util.NetConfig;
import utils.eventbus.EventBus;

/**
 *
 * @author hannes
 */
public class ClientHandoverService extends AbstractClientService implements HandoverSessionEmitter{

    private final List<HandoverSessionListener> listeners = new ArrayList<>();
    
    private HandoverSession delegate;
    // Handle to a server side object
    
    private RmiClientService rmiService;
    // Used to sync with server and to acctually send data 
    
    private int channel;
    // Channel we send on, is it a port though?
    private int serverPort;
    
    public ClientHandoverService(int serverPort){
        this.channel = MessageConnection.CHANNEL_DEFAULT_RELIABLE;
        this.serverPort = serverPort;
    }
    
    @Override
    protected void onInitialize(ClientServiceManager serviceManager) {
        rmiService = getService(RmiClientService.class);
        if( rmiService == null ) {
            throw new RuntimeException("HandoverClientService requires RMI service");
        }
        HandoverSessionListener callback = new GameLobbySessionListenerImpl();
        // Share the callback with the server
        rmiService.share((byte)channel, callback, HandoverSessionListener.class);
        
        
    }
    
    public void joinLobby(){
        String ip = null;
        Enumeration e;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.getHostAddress().startsWith("192.168.")) {
                        System.out.println("MyIP: " + i.getHostAddress());
                        ip = i.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(ClientHandoverService.class.getName()).log(Level.SEVERE, null, ex);
        }
        getDelegate().join(-1, ip, serverPort);
    }
    
    private HandoverSession getDelegate(){
        if(delegate == null){
            delegate = NetConfig.getDelegate(rmiService, HandoverSession.class);
        }
        return delegate;
    }

    @Override
    public void addListener(HandoverSessionListener handoverSessionListener) {
        listeners.add(handoverSessionListener);
    }
    
    
    private class GameLobbySessionListenerImpl implements HandoverSessionListener {

        @Override
        public void startSetup(List<Account> accounts) {
            for (HandoverSessionListener listener : listeners) {
                listener.startSetup(accounts);
            }
        }
        
    }
    
}
