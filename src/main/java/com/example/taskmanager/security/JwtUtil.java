package com.example.taskmanager.security;

import com.example.taskmanager.enums.RoleEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value( "${jwt.secret}")
    private String secretKey;

    public String generateToken(String email, RoleEnum role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Validate the token. It will basically reverse the token data
    public String extractEmail(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)  // validates the signature and validates the expiration date of the token
                .getPayload()              // the data entered inside the token
                .getSubject();             // because we placed the email inside the subject. If we wanted ex.'role' that is present inside the claim() -> get("role")
    }

    // Fetch Roles
    public RoleEnum extractRole(String token) {
        return RoleEnum.valueOf(
                Jwts.parser()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .get("role")
                        .toString()
        );
    }
}
