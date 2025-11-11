package com.example.onlineshoppingapp.service;

import com.example.onlineshoppingapp.dao.UserDAO;
import com.example.onlineshoppingapp.domain.User;
import com.example.onlineshoppingapp.security.AuthUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userDAO.findByUsername(username);

        if (!userOptional.isPresent()){
            throw new UsernameNotFoundException("Username does not exist");
        }

        User user = userOptional.get(); // database user

        return AuthUserDetail.builder() // spring security's userDetail
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(getAuthoritiesFromUser(user))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .isAdmin(user.getIsAdmin())
                .build();
    }

    private List<GrantedAuthority> getAuthoritiesFromUser(User user){
        List<GrantedAuthority> userAuthorities = new ArrayList<>();

        userAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Grant admin role if applicable
        if (user.getIsAdmin()) {
            userAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }


        return userAuthorities;
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