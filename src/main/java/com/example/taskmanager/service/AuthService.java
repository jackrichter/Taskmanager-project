package com.example.taskmanager.service;

import com.example.taskmanager.dto.AuthRequestDto;
import com.example.taskmanager.enums.RoleEnum;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * SECURITY
     */

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Password Validation
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String hashed = passwordEncoder.encode("password123");

        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        if (!passwordMatches) {
            throw new RuntimeException("Invalid credentials");
        }

        // If we reach here, -> login successful
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    public void signup(AuthRequestDto request) {

        // If the User exists already in the database, we can't register him again
        if ( userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create the User
        User user = new User();
        user.setName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(RoleEnum.USER);

        // Save the User
        userRepository.save(user);
    }
}
