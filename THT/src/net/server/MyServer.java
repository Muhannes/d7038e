/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import com.jme3.system.JmeContext;
import java.io.IOException;

/**
 * Only used to demostrate HostedLoginService
 * @author truls
 */
public class MyServer extends SimpleApplication {

    Server server;
    
    @Override
    public void simpleInitApp() {
        initServer();
    }
    
    @Override
    public void destroy(){
        server.close();
        super.destroy();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initServer(){
        try {
            System.out.println("Using port " + 11444);
            // create and start the server
            server = Network.createServer(11444);
        
            System.out.println("Adding services");
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedLoginService());
            server.getServices().addService(new HostedChatService());
            
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            destroy();
        }
        System.out.println("Server started");
    }
    
    public static void main(String[] args){
        MyServer s = new MyServer();
        s.start(JmeContext.Type.Headless);
    }
    
}
