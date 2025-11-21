package com.example.onlineshoppingapp.exception;

// --- Custom Exception ---
// Note: This should ideally be defined in the service layer or a dedicated exception package
public class NotEnoughInventoryException extends RuntimeException {
    public NotEnoughInventoryException(String message) {
        super(message);
    }
}
