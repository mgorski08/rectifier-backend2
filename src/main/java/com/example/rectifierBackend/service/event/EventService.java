package com.example.rectifierBackend.service.event;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@Service
public class EventService {

    private final Set<BlockingQueue<Event<?>>> listeners = new HashSet<>();

    public void registerListener(BlockingQueue<Event<?>> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(BlockingQueue<Event<?>> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void dispatchEvent(Event<?> event) {
        synchronized (listeners) {
            for (BlockingQueue<Event<?>> listener : listeners) {
                listener.add(event);
            }
        }
    }
}
