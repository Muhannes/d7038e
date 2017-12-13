/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hannes
 */
public class ServerBootstrapper {
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String args[]){
        //LobbyNetworkHandler lobbyNH = new LobbyNetworkHandler();
        GameNetworkHandler gameNH = new GameNetworkHandler();
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LobbyNetworkHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
