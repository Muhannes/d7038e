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
public interface HandoverSessionEmitter {
    void addListener(HandoverSessionListener handoverSessionListener);
}
