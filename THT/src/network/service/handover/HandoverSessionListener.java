/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.handover;

import com.jme3.network.service.rmi.Asynchronous;
import java.util.List;
import network.service.login.Account;

/**
 *
 * @author hannes
 */
public interface HandoverSessionListener {
    
    @Asynchronous
    void startSetup(List<Account> accounts);
}
