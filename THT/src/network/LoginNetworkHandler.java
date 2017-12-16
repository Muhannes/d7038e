/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.service.rmi.RmiHostedService;
import com.jme3.network.service.rpc.RpcHostedService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.services.login.server.HostedLoginService;
import network.util.NetConfig;
import static network.util.NetConfig.initSerializables;

/**
 *
 * @author hannes
 */
public class LoginNetworkHandler {
    
    private static final Logger LOGGER = Logger.getLogger(LoginNetworkHandler.class.getName());
    
    private Server server;
    
    public LoginNetworkHandler(){
        Logger.getLogger("").setLevel(Level.INFO);
        initSerializables();
        initLoginServer();
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private void initLoginServer(){
        try {
            //LOGGER.log(Level.INFO, "Starting server at port: {0}", Integer.toString(NetConfig.LOGIN_SERVER_PORT));
            // create and start the server
            server = Network.createServer(NetConfig.LOGIN_SERVER_PORT);
            //server.getServices().removeService(server.getServices().getService(ServerSerializerRegistrationsService.class));
            server.getServices().addService(new RpcHostedService());
            server.getServices().addService(new RmiHostedService());
            server.getServices().addService(new HostedLoginService());
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
            server.close();
        }
    }
    
    public synchronized void cleanUp(){
        if (server != null && server.isRunning()) {
            server.close();
        }
    }
   
   public void startServer(){
       server.start();
   }
    
}

