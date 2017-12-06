/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.services.login;

import com.jme3.network.HostedConnection;
import utils.eventbus.Event;

/**
 *
 * @author hannes
 */
public class LoginEvent extends Event {
    public final HostedConnection conn;
    public LoginEvent(HostedConnection conn){
        this.conn = conn;
    }
}
