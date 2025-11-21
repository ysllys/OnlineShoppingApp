package com.example.onlineshoppingapp.exception;

// Define a simple runtime exception for better error handling in the Controller
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
