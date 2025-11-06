package com.example.onlineshoppingapp.controller;

import com.example.onlineshoppingapp.domain.User;
import com.example.onlineshoppingapp.dto.RegisterRequest;
import com.example.onlineshoppingapp.service.UserService;
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
    public ResponseEntity<User> registerUser(@RequestBody RegisterRequest registerRequest) {
        // Basic input validation: ensure required fields are present
        // Create the User entity from the DTO
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(registerRequest.getPassword());

        User registeredUser = userService.registerUser(user);
        // Return the User entity (or a UserResponse DTO)
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
}