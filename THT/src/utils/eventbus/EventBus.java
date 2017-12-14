/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author hannes
 */
public class EventBus {
    private static EventBus eventBus;
    private List<EventListener> listeners;
    ExecutorService executor;
    private EventBus(){
        listeners = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
    }
    
    private void cleanupEventBus(){
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    private static EventBus getEventBus(){
        if (eventBus == null) {
            eventBus = new EventBus();
        }
        return eventBus;
    } 
    
    private synchronized void publishEvent(Event event, Class<? extends Event> T){
        System.out.println("in method publishEvent");
        for (EventListener listener : listeners) {
            executor.submit(() -> {
                System.out.println("Sending out new event to listeners");
                listener.notifyEvent(event, T);
            });
        }
    }
    
    private void subscribeEvents(EventListener eventListener){
        listeners.add(eventListener);
    }
    
    public static void publish(Event event, Class<? extends Event> T){
        System.out.println("PUBLISH NEW EVENT! type: " + T);
        getEventBus().publishEvent(event, T);
    }
    
    public static void subscribe(EventListener eventListener){
        getEventBus().subscribeEvents(eventListener);
    }
    
    public static void cleanup(){
        getEventBus().cleanupEventBus();
    }
}
