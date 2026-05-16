package com.example.taskmanager.integration;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest            // Spring Boot create a new Application Context between test methods. For real HTTP requests.
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User savedUser = new User();
        savedUser.setName("gilbert");
        savedUser.setEmail("gilter@example.com");
        userRepository.save(savedUser);
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() throws Exception {
        mockMvc.perform(get("/user/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("gilbert"));
    }

    @Test
    void getUserById_shouldReturn404_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/user/{id}", 9999))
                .andExpect(status().isNotFound());
    }
}
