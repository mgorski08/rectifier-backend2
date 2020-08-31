package com.example.rectifierBackend.service;

import com.example.rectifierBackend.driver.RectifierDriver;
import com.example.rectifierBackend.model.Process;
import com.example.rectifierBackend.model.Sample;
import com.example.rectifierBackend.repository.ProcessRepository;
import com.example.rectifierBackend.repository.SampleRepository;
import com.example.rectifierBackend.service.event.Event;
import com.example.rectifierBackend.service.event.EventService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RectifierService {
    private final SampleRepository sampleRepository;
    private final ProcessRepository processRepository;
    private final RectifierDriver rectifierDriver;
    private final EventService eventService;

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    RectifierService(SampleRepository sampleRepository, ProcessRepository processRepository, @Qualifier(value = "mock"
    ) RectifierDriver rectifierDriver, EventService eventService) {
        this.sampleRepository = sampleRepository;
        this.processRepository = processRepository;
        this.rectifierDriver = rectifierDriver;
        this.eventService = eventService;
    }

    @Scheduled(fixedDelay = 100)
    public void queryBaths() {
        for (int i = 1; i < 8; ++i) {
            Sample sample = rectifierDriver.readSample(i);
            sample.setBathId(i);
            eventService.dispatchEvent(new Event<>(Event.SAMPLE_COLLECTED, sample));
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void startProcess(long processId) {
        Process process = processRepository.findById(processId).orElseThrow(() -> new RuntimeException("Process " +
                "doesn't exist."));
        Thread processThread = new Thread(() -> {
            BlockingQueue<Event<?>> listener = new LinkedBlockingQueue<>();
            eventService.registerListener(listener);
            Event<?> event = null;
            try {
                do {
                    event = listener.take();
                    if (event.getObject() instanceof Sample) {
                        Sample sample = (Sample) event.getObject();
                        if(sample.getBathId() == process.getBathId()) {
                            sample.setProcess(process);
                            sampleRepository.save(sample);
                        }
                    }
                } while (!event.getType().equals(Event.PROCESS_STOPPED));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        processThread.start();
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
