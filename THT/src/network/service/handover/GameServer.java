/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.handover;

/**
 *
 * @author hannes
 */
class GameServer {
    public String ipAddress;
    public int port;
    public HandoverSessionListener gameCallback;

    public GameServer(String ipAddress, int port, HandoverSessionListener gameCallback) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.gameCallback = gameCallback;
    }
    
    
}
