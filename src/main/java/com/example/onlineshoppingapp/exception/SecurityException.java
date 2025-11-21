package com.example.onlineshoppingapp.exception;
import org.springframework.security.access.AccessDeniedException;

// --- Helper classes (Should be moved to a separate package in a real app) ---
public class SecurityException extends AccessDeniedException {
    public SecurityException(String message) {
        super(message);
    }
}
