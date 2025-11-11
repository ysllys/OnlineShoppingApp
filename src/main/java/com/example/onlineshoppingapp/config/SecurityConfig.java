package com.example.onlineshoppingapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.example.onlineshoppingapp.security.JwtAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Expose the AuthenticationManager Bean
    // This resolves the autowiring error in AuthService.
    private final UserDetailsService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserDetailsService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Defines the standard password encoder bean.
     * This encoder is used by the DaoAuthenticationProvider to check passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the Authentication Provider, which links the UserDetailsService
     * and PasswordEncoder to the authentication flow.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    /**
     * Configures the security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. Disable CSRF (Common for JWT/REST APIs)
        http.csrf(csrf -> csrf.disable());

        // 2. Configure Session Management to STATELESS (Essential for JWT)
        // JWT tokens replace sessions, so we tell Spring not to create them.
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 3. Configure Authorization Rules
        http.authorizeHttpRequests(auth -> auth
                // Permit public access to registration and login/token generation endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Require authentication for ALL other requests (like ProductController's GET)
                .anyRequest().authenticated()
        );

        // 4. Add the Custom JWT filter BEFORE the standard Spring Authentication filter
        // This ensures the user is identified by the token before the security checks run.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 5. Explicitly add the custom AuthenticationProvider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}