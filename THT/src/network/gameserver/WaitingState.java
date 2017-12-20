/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.gameserver;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.Map;
import network.service.handover.HandoverSessionListener;

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
    }

    @Override
    protected void onDisable() {
        app.getNetworkHandler().disconnectFromLobbyServer();
    }

    @Override
    public void startSetup(Map<Integer, String> playerInfo) {
        SetupState ss = app.getStateManager().getState(SetupState.class);
        //ss.setPlayerInfo(playerInfo);
        this.setEnabled(false);
        app.enqueue(new Runnable() {
            @Override
            public void run() {
                ss.setEnabled(true);
            }
        });
    }
    
}