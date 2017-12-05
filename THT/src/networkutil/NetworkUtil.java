/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import api.models.LobbyRoom;
import com.jme3.network.serializing.Serializer;

/**
 *
 * @author hannes
 */
public class NetworkUtil {
    
    public static void initSerializables(){
        Serializer.registerClass(LobbyRoom.class);
    }
}
