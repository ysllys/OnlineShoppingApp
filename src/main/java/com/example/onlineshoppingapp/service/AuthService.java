package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.security.JwtTokenProvider; // Assume this class exists
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider; // Custom utility for JWT generation
 
    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Authenticates the user credentials and generates a JWT upon success.
     * @param username The user's username.
     * @param password The user's password.
     * @return The generated JWT string.
     */
    public String authenticateAndGenerateToken(String username, String password) {

        // 1. Authenticate the user against the database/UserDetailsService
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. If authentication is successful, set the Authentication object in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate the JWT using the custom provider
        // The token will contain the username and roles.
        String jwt = tokenProvider.generateToken(authentication);

        return jwt;
    }
}