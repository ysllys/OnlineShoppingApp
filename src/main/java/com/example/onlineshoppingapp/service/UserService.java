package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.dao.UserDAO;
import com.example.onlineshoppingapp.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user account, enforcing unique username and email constraints.
     * @param user The User entity containing username, email, and passwordHash.
     * @return The saved User entity.
     * @throws RegistrationException if username or email already exists.
     */
    @Transactional
    public User registerUser(User user) {
        // 1. Enforce Uniqueness Constraints
        if (userDAO.findByUsername(user.getUsername()).isPresent()) {
            throw new RegistrationException("Username is already taken: " + user.getUsername());
        }

        if (userDAO.findByEmail(user.getEmail()).isPresent()) {
            throw new RegistrationException("Email is already registered: " + user.getEmail());
        }

        // 2. Enforce Role Constraint: Only registers standard users
        user.setIsAdmin(false);

        // 3. Save the User (DAO.save returns the persisted entity with ID)
        return userDAO.save(user);
    }
    @Transactional(readOnly = true)
    public Optional<User> findById(Integer userId) {
        return userDAO.findById(userId);
    }
    @Transactional(readOnly = true)
    public Integer getUserIdByUsername(String username) {
        // Use the DAO to find the User entity
        Optional<User> userOptional = userDAO.findByUsername(username);

        // Map the Optional<User> to Optional<Integer> (the ID)
        return userOptional
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for username: " + username));
    }

    // --- Custom Exception for Registration Errors ---

    // Define a custom runtime exception for registration failures
    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }
    }
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message);}
    }
}