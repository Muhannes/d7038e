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
class GameServer {
    public String ipAddress;
    public int port;

    public GameServer(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }
    
    
}
