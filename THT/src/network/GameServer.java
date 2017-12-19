/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.app.SimpleApplication;
import com.jme3.system.JmeContext;
import java.util.logging.Logger;

/**
 *
 * @author hannes
 */
public class GameServer extends SimpleApplication{
    
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        GameServer gameServer = new GameServer();
        GameNetworkHandler gnh = new GameNetworkHandler();
        gnh.startServer();
        gnh.connectToLobbyServer();
        gnh.connectToLoginServer();
        gameServer.start(JmeContext.Type.Headless);
    }
    
    @Override
    public void simpleInitApp() {
        // Do intialization here.
    }
    
}
