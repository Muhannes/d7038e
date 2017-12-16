/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.util.logging.Level;
import java.util.logging.Logger;
import network.util.NetConfig;

/**
 *
 * @author hannes
 */
public class ServerBootstrapper {
    
    private static final Logger LOGGER = Logger.getLogger(ServerBootstrapper.class.getName());
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        LoginNetworkHandler loginNH = new LoginNetworkHandler();
        LobbyNetworkHandler lobbyNH = new LobbyNetworkHandler();
        loginNH.startServer();
        lobbyNH.startServers();
        NetConfig.networkDelay(30);
        lobbyNH.connectToLoginServer();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
