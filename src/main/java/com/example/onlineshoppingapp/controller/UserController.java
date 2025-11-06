package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.User;
import com.example.onlineshoppingapp.service.UserService;
import com.example.onlineshoppingapp.service.UserService.RegistrationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// @RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for user registration.
     * URL: POST /api/users/register
     * @param user The User entity (containing username, email, passwordHash) from the request body.
     * @return 201 Created on success, 409 Conflict on duplicate data.
     */
    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // Basic input validation: ensure required fields are present
        if (user.getUsername() == null || user.getEmail() == null || user.getPasswordHash() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400
        }

        try {
            // Service handles business rules and persistence
            User registeredUser = userService.registerUser(user);

            // Return 201 Created status with the newly registered user
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (RegistrationException e) {
            // Map our custom business exception to a 409 Conflict (Duplicate Resource)
            // Note: In a production app, we'd use a @ControllerAdvice for centralized handling.
            System.err.println("Registration failed: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409
        }
    }
}