/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.util;

import api.models.LobbyRoom;
import api.models.Player;
import com.jme3.network.serializing.Serializer;
import java.util.logging.Level;

/**
 *
 * @author truls
 */
public class NetConfig {
    public static String LOBBY_SERVER_NAME = "localhost";
    public static int LOBBY_PLAYER_SERVER_PORT = 7999;
    public static int LOBBY_HANDOVER_SERVER_PORT = 8000;
    
    public static String GAME_SERVER_NAME = "localhost";
    public static int GAME_SERVER_PORT = 8001;
    
    public static void initSerializables(){
        Serializer.registerClass(LobbyRoom.class);
        Serializer.registerClass(Player.class);
    }
    
    public static void networkDelay(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
    }
}
