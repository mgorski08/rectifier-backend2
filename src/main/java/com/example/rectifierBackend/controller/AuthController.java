package com.example.rectifierBackend.controller;

import com.example.rectifierBackend.message.request.LoginForm;
import com.example.rectifierBackend.message.request.SignUpForm;
import com.example.rectifierBackend.message.response.JwtResponse;
import com.example.rectifierBackend.message.response.ResponseMessage;
import com.example.rectifierBackend.model.User;
import com.example.rectifierBackend.repository.UserRepository;
import com.example.rectifierBackend.security.jwt.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.Set;

@RequestMapping("/auth")
@RestController
@CrossOrigin
public class AuthController {
    private static final long JWT_EXPIRATION = 3600000; //milliseconds

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername()
                        , loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        long iss = new Date().getTime();
        long exp = iss + JWT_EXPIRATION;

        String jwt = jwtProvider.generateJwtToken(authentication, iss, exp);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), exp, userDetails.getAuthorities()));
    }

}
