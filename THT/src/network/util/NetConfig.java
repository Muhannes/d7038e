/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.util;

import api.models.Player;
import com.jme3.network.serializing.Serializer;
import com.jme3.network.service.rmi.RmiClientService;
import com.jme3.network.service.rmi.RmiRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.service.lobby.server.LobbyRoomMessage;
import network.service.login.Account;
import network.service.movement.PlayerMovement;

/**
 *
 * @author truls
 */
public class NetConfig {
    
    private static final Logger LOGGER = Logger.getLogger(NetConfig.class.getName());
    private static final String SERVER_IP = "10.0.0.5";
    
    public static String LOBBY_SERVER_NAME = SERVER_IP;
    public static int LOBBY_PLAYER_SERVER_PORT = 7999;
    public static int LOBBY_HANDOVER_SERVER_PORT = 8000;
    
    public static String GAME_SERVER_NAME = SERVER_IP;
    public static int GAME_SERVER_PORT = 8001;
    
    public static String LOGIN_SERVER_NAME = SERVER_IP;
    public static int LOGIN_SERVER_PORT = 8002;
    
    public static String CHAT_SERVER_NAME = SERVER_IP;
    public static int CHAT_SERVER_PORT = 8003;
    
    public static void initSerializables(){
        Serializer.registerClass(Player.class);
        Serializer.registerClass(PlayerMovement.class);
        Serializer.registerClass(LobbyRoomMessage.class);
        Serializer.registerClass(Account.class);
    }
    
    public static void networkDelay(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public static <T> T getDelegate(RmiClientService rmi, Class<T> type ){
        T delegate = rmi.getRemoteObject(type);
        int counter = 0;
        while (delegate == null){
            networkDelay(50);
            delegate = rmi.getRemoteObject(type); 
            if (counter > 40) { 
                throw new RuntimeException("Unable to locate delegate for " + type.getName());
            }
            counter++;
        }
        return delegate;
    }
    
    public static <T> T getCallback(RmiRegistry rmi, Class<T> type ){
        T callback = rmi.getRemoteObject(type);
        int counter = 0;
        while (callback == null){
            networkDelay(50);
            callback = rmi.getRemoteObject(type); 
            if (counter > 30) {
                throw new RuntimeException("Unable to locate callback for " + type.getName());
            }
            counter++;
        }
        return callback;
    }
    
}
