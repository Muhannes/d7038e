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
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        LoginNetworkHandler loginNH = new LoginNetworkHandler();
        LobbyNetworkHandler lobbyNH = new LobbyNetworkHandler();
        //GameNetworkHandler gameNH = new GameNetworkHandler();
        loginNH.startServer();
        lobbyNH.startServers();
        //gameNH.startServer();
        NetConfig.networkDelay(30);
        lobbyNH.connectToLoginServer();
        //gameNH.connectToLobbyServer();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LobbyNetworkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
