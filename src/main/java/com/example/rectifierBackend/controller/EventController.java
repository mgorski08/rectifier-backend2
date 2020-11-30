package com.example.rectifierBackend.controller;

import com.example.rectifierBackend.service.event.Event;
import com.example.rectifierBackend.service.event.EventService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RequestMapping("/event")
@RestController
@CrossOrigin
public class EventController {

    private final Log logger = LogFactory.getLog(getClass());
    private final EventService eventService;

    EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(value = "liveEvents", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> liveEvents() {
        StreamingResponseBody responseBody = (OutputStream outputStream) -> {
            Event<?> event;
            BlockingQueue<Event<?>> listener = new LinkedBlockingQueue<>();
            eventService.registerListener(listener);
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);
            jsonGenerator.setCodec(new ObjectMapper());
            try {
                do {
                    event = listener.take();
                    jsonGenerator.writeRaw("event:" + event.getType() + "\n");
                    jsonGenerator.writeRaw("data:");
                    jsonGenerator.writeObject(event.getObject());
                    jsonGenerator.writeRaw("\n\n");
                    jsonGenerator.flush();
                } while (!event.getType().equals("End"));
            } catch (ClientAbortException cae) {
                logger.debug("Client disconnected while streaming.");
            } catch (Exception e) {
                logger.error("Error while streaming.", e);
            }
            outputStream.flush();
            outputStream.close();
            eventService.removeListener(listener);
        };
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(responseBody);
    }

}

