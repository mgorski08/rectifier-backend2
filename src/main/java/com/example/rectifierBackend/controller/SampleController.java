package com.example.rectifierBackend.controller;

import com.example.rectifierBackend.model.Sample;
import com.example.rectifierBackend.repository.SampleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/sample")
@RestController
@CrossOrigin
public class SampleController {

    private final SampleRepository sampleRepository;

    public SampleController(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @GetMapping("{sampleId}")
    ResponseEntity<?> getOne(@PathVariable long sampleId) {
        Sample sample = sampleRepository
                .findById(sampleId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample not found.")
                );
        return ResponseEntity.ok(sample);
    }
}
