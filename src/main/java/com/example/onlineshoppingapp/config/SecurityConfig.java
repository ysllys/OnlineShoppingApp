package com.example.onlineshoppingapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🔑 1. Expose the AuthenticationManager Bean
    // This resolves the autowiring error in AuthService.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 🔒 2. Define the PasswordEncoder
    // REQUIRED for the AuthenticationManager to compare passwords securely.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Standard industry choice for hashing passwords
    }

    // ⚙️ 3. Configure the Security Filter Chain (Minimal example for JWT)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Since we are using JWT (stateless), disable CSRF protection
                .csrf(csrf -> csrf.disable())

                // Define access rules (e.g., /api/auth/login and /api/users/register are public)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/users/register", "/api/products/**").permitAll()
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                );

        // NOTE: You would add JWT filtering and session management here in a complete setup.

        return http.build();
    }
}