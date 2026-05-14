package com.example.taskmanager.integration;

import com.example.taskmanager.dto.UserDtoPrevious;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest             // Makes Spring Boot create the Application Context once and use it between test methods => means using the same cache!
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)   // Very Important! Forces a new context to be created for each test method. Cleans the cache after each method!
public class UserControllerPrevIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static UserDtoPrevious createUserDto(String name, String email, Integer age) {
        UserDtoPrevious userDto = new UserDtoPrevious();
        userDto.setName(name);
        userDto.setEmail(email);
        userDto.setAge(age);

        return userDto;
    }

    @Test
    void createUser_andFetchItSuccessfully() throws Exception {
        UserDtoPrevious userDto = createUserDto("Adam", "adam@test.com", 25);

        // Create user in db
        mockMvc.perform(post("/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        // Get all users
        mockMvc.perform(get("/user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Adam"))
                .andExpect(jsonPath("$[0].email").value("adam@test.com"));

        // Get user by id
        mockMvc.perform(get("/user2/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("adam@test.com"));

        // Get user by email
        mockMvc.perform(get("/user2/email/{email}", "adam@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("adam@test.com"))
                .andExpect(jsonPath("$[0].name").value("Adam"));

        // Check if email exists
        mockMvc.perform(get("/user2/exists/{email}", "adam@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));       // This is how to check a boolean value in a test

    }

    @Test
    void createUser_shouldFailValidation () throws Exception {
        // Empty userDto
        UserDtoPrevious userDto = new UserDtoPrevious();

        // User with no email or name should fail validation
        mockMvc.perform(post("/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateAndDeleteUser_endToEnd() throws Exception {
        UserDtoPrevious dto = createUserDto("Sara", "sara@test.com", 30);

        // Post user to db
        mockMvc.perform(post("/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Get user by id
        mockMvc.perform(get("/user2/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sara"))
                .andExpect(jsonPath("$.age").value(30));

        // Update users' age by users' email
        mockMvc.perform(put("/user2/update/email/{email}/age/{age}", "sara@test.com", 35))
                .andExpect(status().isNoContent());

        // Retrieve updated user
        mockMvc.perform(get("/user2/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sara"))
                .andExpect(jsonPath("$.age").value(35));

        // Delete user
        mockMvc.perform(delete("/user2/{id}", 1))
                .andExpect(status().isNoContent());

        // User should not be found
        mockMvc.perform(get("/user2/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getInexistentUser_shouldFail() throws Exception {
        mockMvc.perform(get("/user2/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"));   // Message from UserNotFoundException
    }
}
