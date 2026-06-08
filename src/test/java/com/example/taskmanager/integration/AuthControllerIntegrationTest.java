package com.example.taskmanager.integration;

import com.example.taskmanager.dto.AuthRequestDto;
import com.example.taskmanager.enums.RoleEnum;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void signupEndPoint_successful() throws Exception {
        // Prepare data (Given)
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("integration@example.com");
        requestDto.setPassword("password");
        requestDto.setUsername("integrationUser");

        // Execute the request (Then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void signupEndPoint_userAlreadyExists() throws Exception {
        // Prepare data (Given)
        User existingUser = new User();
        existingUser.setEmail("integration@example.com");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setName("Existing User");
        existingUser.setRole(RoleEnum.USER);
        userRepository.save(existingUser);

        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("integration@example.com");
        requestDto.setPassword("password");
        requestDto.setUsername("integrationUser");

        // Execute the request (Then)
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void loginEndPoint_successful() throws Exception {
        // Prepare data (Given)
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setName("Login User");
        user.setRole(RoleEnum.USER);
        userRepository.save(user);

        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("login@example.com");
        requestDto.setPassword("password");

        // Execute the request (Then)
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void loginEndPoint_invalidCredentials() throws Exception {
        // Given
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("nonexistent@example.com");
        requestDto.setPassword("wrong");

        // Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }
}
