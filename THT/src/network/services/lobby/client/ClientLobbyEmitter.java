/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.lobby.client;

import network.services.lobby.LobbySessionListener;

/**
 *
 * @author hannes
 */
public interface ClientLobbyEmitter {
    void addClientLobbyListener(LobbySessionListener clientLobbyListener);
    void removeClientLobbyListener(LobbySessionListener clientLobbyListener);
}
