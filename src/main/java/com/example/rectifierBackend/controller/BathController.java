package com.example.rectifierBackend.controller;

import com.example.rectifierBackend.model.Bath;
import com.example.rectifierBackend.model.User;
import com.example.rectifierBackend.repository.BathRepository;
import com.example.rectifierBackend.repository.ProcessRepository;
import com.example.rectifierBackend.repository.UserRepository;
import com.fazecast.jSerialComm.SerialPort;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/bath")
@RestController
@CrossOrigin
public class BathController {

    private final BathRepository bathRepository;

    public BathController(BathRepository bathRepository) {
        this.bathRepository = bathRepository;
    }

    @GetMapping("{bathId}")
    ResponseEntity<?> getOne(@PathVariable long bathId) {
        Bath bath = bathRepository
                .findById(bathId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bath not found.")
                );
        return new ResponseEntity<>(bath, HttpStatus.OK);
    }

    @GetMapping("")
    ResponseEntity<?> getAll() {
        return ResponseEntity.ok(bathRepository.findAllByOrderByIdAsc());
    }

    @DeleteMapping("{bathId}")
    ResponseEntity<?> delete(@PathVariable long bathId) {
        try {
            bathRepository.deleteById(bathId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<Bath>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("{bathId}/occupy")
    ResponseEntity<?> occupy(@PathVariable long bathId) {
        User user = User.getCurrentUser().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
        );
        Bath bath = bathRepository.findById(bathId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bath not found.")
        );
        if(bath.getUser() != null && bath.getUser().getId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bath already occupied by a different user.");
        }
        bath.setUser(user);
        bathRepository.save(bath);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{bathId}/free")
    ResponseEntity<?> free(@PathVariable long bathId) {
        Bath bath = bathRepository.findById(bathId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bath not found.")
        );
        if(bath.getProcess() != null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot free bath, when a process is running"
            );
        }
        bath.setUser(null);
        bathRepository.save(bath);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("")
    ResponseEntity<?> add(@RequestBody Bath bath) {
        bathRepository.save(bath);
        return ResponseEntity.ok(bath);
    }

}
