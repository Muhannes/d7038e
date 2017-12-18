/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.movement.server;

import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rmi.RmiRegistry;
import network.service.login.Account;
import network.service.movement.MovementSession;
import network.service.movement.MovementSessionListener;
import network.util.NetConfig;

/**
 *
 * @author ted
 */
public class MovementSessionImpl implements MovementSession, 
        MovementSessionListener{

    private final HostedConnection conn;
    private MovementSessionListener callback;
    private final RmiHostedService rmi;
    private Account account;
    private boolean authenticated;

    MovementSessionImpl(HostedConnection conn, RmiHostedService rmi){
        this.conn = conn;
        this.rmi = rmi;
    }
    
    @Override
    public void sendMessage(Vector3f location, int id) {
        if (!authenticated) {
            return ;
        }
        MovementSpace.getMovementSpace(id).postMessage(this, location);
    }

    private MovementSessionListener getCallback(){
        if (callback == null){
            RmiRegistry rmiRegistry = rmi.getRmiRegistry(conn);
            callback =  NetConfig.getCallback(rmiRegistry, MovementSessionListener.class);
        }
        return callback;
    }

    @Override
    public void newMessage(Vector3f location, int id) {
        if (!authenticated) {
            return;
        }
        getCallback().newMessage(location, id);
    }

    @Override
    public void joinedMovement(int movementId){
        if (!authenticated) {
            return;
        }
        MovementSpace.getMovementSpace(movementId).add(this);
    }

    @Override
    public void leftMovement(int movementId){
        if (!authenticated) {
            return;
        }
        MovementSpace.getMovementSpace(movementId).remove(this);
    }

    @Override
    public void playerJoinedMovement(String name, int movementId) {
        if (!authenticated) {
            return;
        }
        getCallback().playerJoinedMovement(name, movementId);
    }

    @Override
    public void playerLeftMovement(String name, int movementId) {
        if (!authenticated) {
            return;
        }
        getCallback().playerLeftMovement(name, movementId);
    }

    String getName(){
        if (!authenticated) {
            return null;
        }
        return account.name;
    }

    
}
