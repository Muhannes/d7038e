/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

/**
 *
 * @author truls
 */
public class LoginAckMessage extends AbstractTCPMessage{

    public boolean accepted;
    
    public LoginAckMessage(){}
    
    public LoginAckMessage(boolean accepted){
        this.accepted = accepted;
    }
}
