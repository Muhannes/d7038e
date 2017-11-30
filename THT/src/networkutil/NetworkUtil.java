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
        // TODO: register for all new message types
        /*Serializer.registerClass(DiskStateMessage.class);
        Serializer.registerClass(GameStateMessage.class);
        Serializer.registerClass(InitMessage.class);
        Serializer.registerClass(InitAckMessage.class);
        Serializer.registerClass(JoinMessage.class);
        Serializer.registerClass(JoinAckMessage.class);
        Serializer.registerClass(StartMessage.class);
        Serializer.registerClass(PlayerMoveMessage.class);
        Serializer.registerClass(DiskStateMessage.class);
        Serializer.registerClass(RequestStartMessage.class);
        Serializer.registerClass(DiskState.class);
        Serializer.registerClass(TimeMessage.class);
*/
    }
}
