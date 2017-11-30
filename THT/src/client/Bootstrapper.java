/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import client.network.ClientNetworkHandler;

/**
 *
 * @author truls
 */
public class Bootstrapper {
    public static void main(String[] args){
        ClientApplication clientApplication = new ClientApplication();
        ClientNetworkHandler clientNetworkHandler = new ClientNetworkHandler();
        
        clientApplication.addLobbySelectionListener(clientNetworkHandler);
        
        clientNetworkHandler.addLobbyListener(clientApplication);
        clientNetworkHandler.addPlayerConnectionListener(clientApplication);
                
        clientApplication.start();
    }
}
