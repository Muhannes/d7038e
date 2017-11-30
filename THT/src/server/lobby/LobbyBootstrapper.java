/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import server.lobby.network.NetworkHandler;

/**
 *
 * @author hannes
 */
public class LobbyBootstrapper {
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) throws InterruptedException{
        
        NetworkHandler networkHandler = new NetworkHandler();
        LobbyApplication lobbyApplication = new LobbyApplication(networkHandler);
        //TODO: connect listeners
        networkHandler.addConnectionListener(lobbyApplication);
        networkHandler.addLobbySelectionListener(lobbyApplication);
        lobbyApplication.addLobbyListener(networkHandler);
        lobbyApplication.addPlayerConnectionListener(networkHandler);
        while(true){//Ugly solution
            Thread.sleep(1000);
        }
    }
    
}
