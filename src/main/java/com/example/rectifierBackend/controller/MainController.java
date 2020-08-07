package com.example.rectifierBackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
@CrossOrigin
public class MainController {

    @GetMapping("")
    ResponseEntity<?> rootPage() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
