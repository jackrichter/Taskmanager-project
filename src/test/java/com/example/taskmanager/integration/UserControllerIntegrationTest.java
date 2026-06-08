package com.example.taskmanager.integration;

import com.example.taskmanager.enums.RoleEnum;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest            // Spring Boot create a new Application Context between test methods. For real HTTP requests.
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)   // Recreates the context before each test method
//@WithMockUser // For Security (In Integration tests only) using the User Default Account provided by Spring Boot
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtTokenProvider;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Create normal user
        User user = new User();
        user.setName("gilbert");
        user.setEmail("gilbert@example.com");
        user.setPassword("gilbert"); // In tests, plain password is fine
        user.setRole(RoleEnum.USER);
        userRepository.save(user);

        // Create admin user
        User admin = new User();
        admin.setName("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("admin");
        admin.setRole(RoleEnum.ADMIN);
        userRepository.save(admin);

        // Generate JWT tokens
        userToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        adminToken = jwtTokenProvider.generateToken(admin.getEmail(), admin.getRole());
    }

    @Test
    void getUserById_shouldReturn200_whenUserExists() throws Exception {
        mockMvc.perform(get("/users/{id}", 1)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("gilbert"));
    }

    @Test
    void getUserById_shouldReturn404_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/{id}", 9999)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_shouldReturn200AndUserList_whenAdmin() throws Exception {
       // Add another User
        User anotherUser  = new User();
        anotherUser.setName("alice");
        anotherUser.setEmail("alice@example.com");
        anotherUser.setPassword("alice123");
        anotherUser.setRole(RoleEnum.USER);
        userRepository.save(anotherUser);

        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("gilbert"))
                .andExpect(jsonPath("$[1].name").value("admin"))
                .andExpect(jsonPath("$[2].name").value("alice"));
    }

    @Test
    void getAllUsers_shouldReturn403_whenNoAdmin() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
