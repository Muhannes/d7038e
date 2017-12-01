/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkutil;

import com.jme3.network.serializing.Serializable;

/**
 *
 * @author truls
 */
@Serializable
public class LoginAckMessage extends AbstractTCPMessage{

    public boolean accepted;
    
    public LoginAckMessage(){}
    
    public LoginAckMessage(boolean accepted){
        this.accepted = accepted;
    }
}
