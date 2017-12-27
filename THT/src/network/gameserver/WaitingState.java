/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.List;
import java.util.Map;
import network.service.handover.HandoverSessionListener;
import network.service.login.Account;

/**
 *
 * @author ted
 */
public class WaitingState extends BaseAppState implements HandoverSessionListener{

    private GameServer app;
    
    @Override
    protected void initialize(Application app) {
        this.app = (GameServer) app;
    }

    @Override
    protected void cleanup(Application app) {
        // TODO: Cleanup!
    }

    @Override
    protected void onEnable() {
        app.getNetworkHandler().connectToLobbyServer();
        app.getNetworkHandler().getClientHandoverService().addListener(this);
    }

    @Override
    protected void onDisable() {
        app.getNetworkHandler().disconnectFromLobbyServer();
    }

    @Override
    public void startSetup(List<Account> accounts) {
        SetupState ss = app.getStateManager().getState(SetupState.class);
        ss.setAccounts(accounts);
        this.setEnabled(false);
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                ss.setEnabled(true);
            }
        });
    }
    
}
