package com.example.rectifierBackend.service.event;

public class Event<T> {
    private final String type;
    private final T object;

    public static final String PROCESS_STARTED = "processStarted";
    public static final String PROCESS_STOPPED = "processStopped";
    public static final String SAMPLE_COLLECTED = "sampleCollected";

    public Event(String type, T object) {
        this.type = type;
        this.object = object;
    }

    public String getType() {
        return type;
    }

    public T getObject() {return object;}
}
