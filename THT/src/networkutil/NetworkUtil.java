/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import com.jme3.network.serializing.Serializer;

/**
 *
 * @author hannes
 */
public class NetworkUtil {
    public static final int LOBBY_SERVER_PORT = 7999;
    
    public static final String SERVER_HOSTNAME = "localhost";
    
    public static void initSerializables(){
        Serializer.registerClass(JoinRoomMessage.class);
        Serializer.registerClass(JoinRoomAckMessage.class);
        Serializer.registerClass(LeaveRoomMessage.class);
        Serializer.registerClass(ReadyMessage.class);
        Serializer.registerClass(LobbyRoomsMessage.class);
    }
}
