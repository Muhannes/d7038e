/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.gamelobbyservice;

/**
 *
 * @author hannes
 */
public interface GameLobbySession {
    boolean join(int key, int port);
}
