/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import com.jme3.system.JmeContext;
import server.lobby.network.NetworkHandler;

/**
 *
 * @author hannes
 */
public class LobbyBootstrapper {
    public LobbyBootstrapper(){
        
        NetworkHandler networkHandler = new NetworkHandler();
        LobbyApplication lobbyApplication = new LobbyApplication(networkHandler);
        //TODO: connect listeners
        networkHandler.addConnectionListener(lobbyApplication);
        networkHandler.addLobbySelectionListener(lobbyApplication);
        lobbyApplication.addLobbyListener(networkHandler);
        lobbyApplication.addPlayerConnectionListener(networkHandler);
        //Start application headless
        lobbyApplication.start(JmeContext.Type.Headless);
    }
    
}
