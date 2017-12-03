/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.network.Client;
import com.jme3.network.Network;
import com.jme3.network.service.rmi.RmiClientService;
import com.jme3.network.service.rpc.RpcClientService;
import java.io.IOException;
import net.ChatSessionListener;
import net.LoginSessionListener;

/**
 * Only used to demostrate ClientLoginService
 * @author truls
 */
public class MyClient extends SimpleApplication implements 
        LoginSessionListener, 
        ChatSessionListener,
        ActionListener{
    
    Client client;

    @Override
    public void simpleInitApp() {
        connectToServer();
        flyCam.setEnabled(false);
        
        inputManager.addMapping("login", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("send", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(this, "login", "send");
        
        client.getServices().getService(ClientLoginService.class).addLoginSessionListener(this);
        client.getServices().getService(ClientChatService.class).addChatSessionListener(this);
    }
    
    @Override
    public void destroy(){
        client.close();
        super.destroy();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void connectToServer(){
           try{
            System.out.println("Trying to connect to server");
            client = Network.connectToServer("localhost", 11444);
            
            // Rpc and Rmi services are required
            client.getServices().addService(new RpcClientService());
            client.getServices().addService(new RmiClientService());
            client.getServices().addService(new ClientLoginService());
            client.getServices().addService(new ClientChatService());
                   
            client.start();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        MyClient c = new MyClient();
        c.start();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if ( name.equals("login") && !isPressed ){
            client.getServices().getService(ClientLoginService.class).login("John Doe");
        }
        
        if ( name.equals("send") && !isPressed ){
            client.getServices().getService(ClientChatService.class).sendMessage("Hello from client " + client.getId());
        }
    }

    @Override
    public void notifyLogin(boolean loggedIn) {
        if(loggedIn){
            System.out.println("Logged in!");
        }else{
            System.out.println("Not logged in!");
        }
    }

    @Override
    public void newMessage(String message) {
        System.out.println("Received messsage: " + message);
    }
}
