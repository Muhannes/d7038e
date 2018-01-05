/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.service.handover;

/**
 *
 * @author hannes
 */
public interface HandoverSession {
    boolean join(int key, String ip, int port);
}
