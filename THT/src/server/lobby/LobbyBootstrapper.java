/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import server.lobby.network.LobbyMessageListener;
import server.lobby.network.NetworkHandler;

/**
 *
 * @author hannes
 */
public class LobbyBootstrapper {
    
    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) throws InterruptedException{
        LobbyMessageListener msgListener = new LobbyMessageListener();
        NetworkHandler networkHandler = new NetworkHandler(msgListener);
        LobbyHolder lobbyHolder = new LobbyHolder();
        LobbyStarter lobbyStarter = new LobbyStarter(lobbyHolder);
        LobbyConnectionHandler lobbyApplication = new LobbyConnectionHandler(networkHandler, lobbyHolder);
        //TODO: connect listeners
        networkHandler.addConnectionListener(lobbyApplication);
        msgListener.addLobbySelectionListener(lobbyApplication);
        msgListener.addPlayerReadyListener(lobbyStarter);
        msgListener.addLoginListener(lobbyApplication);
        lobbyHolder.addLobbyListener(networkHandler);
        lobbyApplication.addPlayerConnectionListener(networkHandler);
        while(true){//Ugly solution
            Thread.sleep(1000);
        }
    }
    
}
