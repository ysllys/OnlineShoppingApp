package com.example.onlineshoppingapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Inject properties from application.properties or application.yml
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    /**
     * Generates a JWT based on the authenticated user's details.
     * @param authentication The Spring Security Authentication object.
     * @return The generated JWT string.
     */
    public String generateToken(Authentication authentication) {

        // Get the username (principal) from the Authentication object
        String username = authentication.getName();

        return Jwts.builder()
                .setSubject(username) // The principal who is the subject of the token
                .signWith(key(), SignatureAlgorithm.HS256) // Sign the JWT with the secret key
                .compact();
    }

    /**
     * Extracts the username from the JWT.
     * @param token The JWT string.
     * @return The username (subject).
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Validates the integrity and expiration of the JWT.
     * @param authToken The JWT string.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    // --- Private Helper Method ---

    /**
     * Converts the base64-encoded secret string into a cryptographic Key object.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}