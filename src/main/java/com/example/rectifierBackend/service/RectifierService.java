package com.example.rectifierBackend.service;

import com.example.rectifierBackend.driver.RectifierDriver;
import com.example.rectifierBackend.model.Bath;
import com.example.rectifierBackend.model.Process;
import com.example.rectifierBackend.model.Sample;
import com.example.rectifierBackend.repository.BathRepository;
import com.example.rectifierBackend.repository.ProcessRepository;
import com.example.rectifierBackend.repository.SampleRepository;
import com.example.rectifierBackend.service.event.Event;
import com.example.rectifierBackend.service.event.EventService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Service
public class RectifierService {
    private final SampleRepository sampleRepository;
    private final ProcessRepository processRepository;
    private final RectifierDriver rectifierDriver;
    private final EventService eventService;

    @Autowired
    RectifierService(SampleRepository sampleRepository,
                     ProcessRepository processRepository, @Qualifier(value = "mock") RectifierDriver rectifierDriver,
                     EventService eventService) {
        this.sampleRepository = sampleRepository;
        this.processRepository = processRepository;
        this.rectifierDriver = rectifierDriver;
        this.eventService = eventService;
    }

    @Scheduled(fixedDelay = 100)
    public void queryBaths() {
        for (int i = 1; i < 8 ; ++i) {
            Sample sample = rectifierDriver.readSample(i);
            sample.setBathId(i);
            eventService.dispatchEvent(new Event<>(Event.SAMPLE_COLLECTED, sample));
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
    }

    public void startProcess(long processId) {
        Process process = processRepository.findById(processId).orElseThrow(() -> new RuntimeException("Process " +
                "doesn't exist."));
        Thread processThread = new Thread(() -> {
            BlockingQueue<Event<?>> listener = new LinkedBlockingQueue<>();
            eventService.registerListener(listener);
            Event<?> event = null;
            do {
                while(event == null) {
                    try {
                        event = listener.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (event.getObject() instanceof Sample) {
                    Sample sample = (Sample) event.getObject();
                    sample.setProcess(process);
                    sampleRepository.save(sample);
                }
            } while (!event.getType().equals(Event.PROCESS_STOPPED));
        });
        process.setStartTimestamp(new Timestamp(System.currentTimeMillis()));
        processRepository.save(process);
        eventService.dispatchEvent(new Event<>(Event.PROCESS_STARTED, process));
    }

    public void stopProcess(long processId) {
        Process process = processRepository.findById(processId).orElseThrow(() -> new RuntimeException("Process " +
                "doesn't exist."));
        process.setStopTimestamp(new Timestamp(System.currentTimeMillis()));
        processRepository.save(process);
        eventService.dispatchEvent(new Event<>(Event.PROCESS_STOPPED, process));
    }


}
