/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils.eventbus;

/**
 *
 * @author hannes
 */
public interface EventListener {
    void notifyEvent(Event event, Class<? extends Event> T);
}
