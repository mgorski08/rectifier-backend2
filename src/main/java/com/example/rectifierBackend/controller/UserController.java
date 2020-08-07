package com.example.rectifierBackend.controller;

import com.example.rectifierBackend.message.request.SignUpForm;
import com.example.rectifierBackend.message.response.ResponseMessage;
import com.example.rectifierBackend.model.User;
import com.example.rectifierBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RequestMapping("/user")
@RestController
@CrossOrigin
public class UserController {

    private static final String DEFAULT_USERNAME = "default";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("")
    ResponseEntity<?> getAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("{userId}")
    ResponseEntity<?> getOne(@PathVariable long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.")
                );
        return ResponseEntity.ok(user);
    }

    @GetMapping("current")
    ResponseEntity<?> getCurrent() {
        return ResponseEntity.ok(User.getCurrentUser().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
        ));
    }

    @PostMapping("")
    ResponseEntity<?> addUser(@Valid @RequestBody SignUpForm signUpForm) {
        if (userRepository.existsByUsername(signUpForm.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken.");
        }
        User user = new User(signUpForm.getUsername(), passwordEncoder.encode(signUpForm.getPassword()));

        Set<String> strRoles = signUpForm.getRoles();

        user.setFirstName(signUpForm.getFirstName());
        user.setLastName(signUpForm.getLastName());
        user.setRoles(strRoles);
        userRepository.save(user);

        return new ResponseEntity<>(new ResponseMessage("User registered successfully."), HttpStatus.OK);

    }

    @GetMapping("defaultUser")
    ResponseEntity<?> addDefaultUser() {
        String defaultPassword = Optional.ofNullable(
                System.getenv("DEFAULT_PASSWORD"))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default password not set")
                );
        userRepository.deleteByUsername(DEFAULT_USERNAME);
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setUsername(DEFAULT_USERNAME);
        signUpForm.setPassword(defaultPassword);
        signUpForm.setRoles(Collections.singleton("ROLE_ADMIN"));
        return addUser(signUpForm);
    }

    @DeleteMapping("{userId}")
    ResponseEntity<?> delete(@PathVariable long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }
}
