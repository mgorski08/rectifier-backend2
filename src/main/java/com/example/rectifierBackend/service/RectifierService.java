package com.example.rectifierBackend.service;

import com.example.rectifierBackend.driver.RectifierDriver;
import com.example.rectifierBackend.model.Process;
import com.example.rectifierBackend.model.Sample;
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
    private static final long SAMPLE_RATE_MS = 2000;
    private final Map<Long, ScheduledFuture<?>> runningProcesses = new HashMap<>();
    private final TaskScheduler taskScheduler;
    private final SampleRepository sampleRepository;
    private final ProcessRepository processRepository;
    private final RectifierDriver rectifierDriver;
    private final EventService eventService;
    private final Map<Long, Set<BlockingQueue<Optional<Sample>>>> bqMap = new HashMap<>();

    @Autowired
    RectifierService(TaskScheduler taskScheduler, SampleRepository sampleRepository,
                     ProcessRepository processRepository, @Qualifier(value = "serial") RectifierDriver rectifierDriver,
                     EventService eventService) {
        this.taskScheduler = taskScheduler;
        this.sampleRepository = sampleRepository;
        this.processRepository = processRepository;
        this.rectifierDriver = rectifierDriver;
        this.eventService = eventService;
    }

    @Scheduled(fixedDelay = 100)
    public void queryBaths() {
        for (int i = 1; i < 8 ; ++i) {
            Sample sample = rectifierDriver.readSample(i);
            eventService.dispatchEvent(new Event<>(Event.SAMPLE_COLLECTED, sample));
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
    }

    public void startProcess(long processId) {
        Process process = processRepository.findById(processId).orElseThrow(() -> new RuntimeException("Process " +
                "doesn't exist."));
        bqMap.put(processId, new HashSet<>());
        ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> {
            Sample sample = rectifierDriver.readSample(process.getBathId());
            sample.setProcess(process);
            sampleRepository.save(sample);
            synchronized (bqMap.get(processId)) {
                for (BlockingQueue<Optional<Sample>> queue : bqMap.get(processId)) {
                    queue.add(Optional.of(sample));
                }
            }
        }, SAMPLE_RATE_MS);
        runningProcesses.put(processId, scheduledFuture);
        process.setStartTimestamp(new Timestamp(System.currentTimeMillis()));
        processRepository.save(process);
        eventService.dispatchEvent(new Event<>(Event.PROCESS_STARTED, process));
    }

    public void stopProcess(long processId) {
        Process process = processRepository.findById(processId).orElseThrow(() -> new RuntimeException("Process " +
                "doesn't exist."));
        ScheduledFuture<?> scheduledFuture = runningProcesses.get(processId);
        process.setStopTimestamp(new Timestamp(System.currentTimeMillis()));
        if (scheduledFuture != null) scheduledFuture.cancel(false);
        if(bqMap.get(processId) != null) {
            synchronized (bqMap.get(processId)) {
                for (BlockingQueue<Optional<Sample>> queue : bqMap.get(processId)) {
                    queue.add(Optional.empty());
                }
                bqMap.remove(processId);
            }
        }
        runningProcesses.remove(processId);
        processRepository.save(process);
        eventService.dispatchEvent(new Event<>(Event.PROCESS_STOPPED, process));
    }
}
