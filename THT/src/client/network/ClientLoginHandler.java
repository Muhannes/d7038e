/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.network;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import networkutil.LoginAckMessage;

/**
 * Handles messaging with the server during login phase
 * @author truls
 */
public class ClientLoginHandler implements MessageListener<Client>{

    ClientLoginHandler() {
        
    }
    
    void initMessageListener(Client client) {
        client.addMessageListener(this, LoginAckMessage.class);
    }

    @Override
    public void messageReceived(Client source, Message m) {
        if(m instanceof LoginAckMessage){
            if(((LoginAckMessage) m).accepted){
                System.out.println("Login accepted");
            }else{
                System.out.println("Login declined");
            }
        }
    }
    
}
