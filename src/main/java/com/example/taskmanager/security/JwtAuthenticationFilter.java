package com.example.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        // We read the token we placed in our request
        String authHeader = request.getHeader("Authorization");

        // We need to validate the token's format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Skip authentication
            filterChain.doFilter(request, response);
            return;
        }

        // We extract the token by itself. We Remove 'Bearer' + 'space' from the string
        String token = authHeader.substring(7);

        // We must verify the signature, expiration date, ensure token integrity and finally extract the email (our mesure of autentication)
        try {
            String email = jwtUtil.extractEmail(token); // If passed, the email is valid, that is the request is valid, and the user is authenticated

            /** Mandatory **/

            // Create a UserAuthentication object based on the valid email
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of());     // Here will be the role that the user has. This list has the Authorization role

            // Attach the UserAuthentication object to the Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
