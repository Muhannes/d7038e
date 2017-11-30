/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

/**
 *
 * @author hannes
 */
public class JoinRoomAckMessage extends AbstractTCPMessage {
    public boolean ok;
    
    public JoinRoomAckMessage() {
    }
    
    public JoinRoomAckMessage(boolean ok){
        this.ok = ok;
    }
}
