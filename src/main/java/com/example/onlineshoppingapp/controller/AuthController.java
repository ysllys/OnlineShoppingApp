package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.service.AuthService;
import com.example.onlineshoppingapp.dto.LoginRequest;    // Assume this exists
import com.example.onlineshoppingapp.dto.JwtAuthResponse; // Assume this exists
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles the user login request.
     * URL: POST /api/auth/login
     * @param loginRequest DTO containing username and password.
     * @return ResponseEntity containing the JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {

        // 1. Service handles authentication and token generation
        String jwt = authService.authenticateAndGenerateToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        // 2. Return the JWT to the client
        return ResponseEntity.ok(new JwtAuthResponse(jwt));
    }
}