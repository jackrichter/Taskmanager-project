package com.example.taskmanager.integration;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.enums.RoleEnum;
import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.TaskStatusRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)   // Recreates the context before each test method
//@WithMockUser // For Security (In Integration tests only) using the User Default Account provided by Spring Boot
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User user;
    private User admin;

    private String userToken;
    private String adminToken;

    private TaskStatus statusNew;
    private TaskStatus statusPending;
    private TaskStatus statusInProgress;
    private TaskStatus statusCompleted;

    @BeforeEach
    void setUp() {
        setTaskStatus();

        // regular user
        user = new User();
        user.setName("gilbert");
        user.setEmail("gilbert@example.com");
        user.setPassword("gilbert");
        user.setRole(RoleEnum.USER);
        userRepository.save(user);

        // admin user
        admin = new User();
        admin.setName("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("admin");
        admin.setRole(RoleEnum.ADMIN);
        userRepository.save(admin);

        userToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole());
    }

    // Creates a task having a User to test and validate Security aspects
    private Task createTaskEntity(String title, User owner) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(title);
        task.setStatus(statusNew);
        task.setUser(owner);

        return taskRepository.save(task);
    }

    /* -------------------------------------------------
       POST /tasks
     ------------------------------------------------- */
    @Test
    void createTask_shouldCreateTask() throws Exception {
        this.createTask(TaskDto.builder()
                .title("New Task")
                .description("Integration test")
                .status(TaskStatusEnum.NEW)
                .build());
    }

    @Test
    void createTask_shouldFailOnValidation() throws Exception {
        TaskDto dto = new TaskDto();        // Missing fields

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    /* -------------------------------------------------
       POST /tasks/upload
     ------------------------------------------------- */
    @Test
    void uploadTask_shouldUploadValidFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                    [
                        {"title": "Task 1", "description": "Description 1", "status": "NEW"},
                        {"title": "Task 2", "description": "Description 2", "status": "NEW"}
                    ]
                """.getBytes()
        );

        // Then
        mockMvc.perform(multipart("/tasks/upload")
                .file(file)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("uploaded: 2 tasks")));
    }

    @Test
    void uploadTask_shouldFailOnValidationErrors() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                   [
                      {"title": ""}
                   ]
                """.getBytes()
        );

        // Then
        mockMvc.perform(multipart("/tasks/upload")
                .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("title: must not be blank")));

    }

    @Test
    void uploadTask_shouldFailOnInvalidFileFormat() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "task.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Invalid file content".getBytes()
        );

        // Then
        mockMvc.perform(multipart("/tasks/upload")
                .file(file)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    /* -------------------------------------------------
       GET /tasks (Pageable)
     ------------------------------------------------- */
    @Test
    void getAllTasks_shouldReturnPagedResult() throws Exception {
        // Then
        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + userToken)
                .param("page", "0")
                .param("size", "2")
                .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size()").value(2));
    }

    /* -------------------------------------------------
       GET /tasks/{id}
     ------------------------------------------------- */
    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        // Given
        String response = createTask(TaskDto.builder()
                .title("Find me")
                .description("Find me")
                .status(TaskStatusEnum.NEW)
                .build());

        TaskDto created = objectMapper.readValue(response, TaskDto.class);

        // Then
        mockMvc.perform(get("/tasks/{id}", created.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void getTaskById_shouldReturn404_whenTaskNotFound() throws Exception {
        // Then
        mockMvc.perform(get("/tasks/{id}", 9999)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    /* -------------------------------------------------
       GET By Id /tasks/{id} with Security
     ------------------------------------------------- */

    @Test
    void getTaskById_shouldReturnTask_whenUserIsOwner() throws Exception {
        // Given
        Task task = createTaskEntity("Find me", user);

        // Then
        mockMvc.perform(get("/tasks/{id}", task.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void getTaskById_shouldReturn403_whenUserIsNotOwner() throws Exception {
        // Given
        Task task = createTaskEntity("Secret task", admin);

        // Then
        mockMvc.perform(get("/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getTaskById_shouldReturnTask_whenAdminAccessAnyTask() throws Exception {
        // Given
        Task task = createTaskEntity("Admin sees me", user);

        // Then
        mockMvc.perform(get("/tasks/{id}", task.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin sees me"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    /* -------------------------------------------------
       PUT /tasks/{id}
     ------------------------------------------------- */
    @Test
    void updateTask_shouldUpdateSuccessfully() throws Exception {
        // Given
        String response = createTask(TaskDto.builder()
                .title("Original")
                .description("Before update")
                .status(TaskStatusEnum.NEW)
                .build());

        TaskDto saved = objectMapper.readValue(response, TaskDto.class);

        // Then
        mockMvc.perform(put("/tasks/{id}", saved.getId())
                        .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                          {
                            "title": "Updated",
                            "description": "After update",
                            "status": "PENDING"
                          }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void updateTask_shouldFailOnValidation() throws Exception {
        // Then
        mockMvc.perform(put("/tasks/{id}", 1)
                        .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /* -------------------------------------------------
       DELETE /tasks/{id}
     ------------------------------------------------- */
    @Test
    void deleteTask_shouldDeleteSuccessfully() throws Exception {
        // Given
        String response = createTask(TaskDto.builder()
                .title("To be deleted")
                .description("Delete me")
                .status(TaskStatusEnum.NEW)
                .build());

        TaskDto created = objectMapper.readValue(response, TaskDto.class);

        // Then
        mockMvc.perform(delete("/tasks/{id}", created.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", created.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    /* -------------------------------------------------
       Create a Task
     ------------------------------------------------- */
    private String createTask(TaskDto dto) throws Exception {
        return mockMvc.perform(post("/tasks")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.status").value(dto.getStatus().name()))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void setTaskStatus() {
        statusNew = new TaskStatus();
        statusNew.setCode(TaskStatusEnum.NEW);
        statusNew.setDescription("New");

        statusPending = new TaskStatus();
        statusPending.setCode(TaskStatusEnum.PENDING);
        statusPending.setDescription("Pending");

        statusInProgress = new TaskStatus();
        statusInProgress.setCode(TaskStatusEnum.IN_PROGRESS);
        statusInProgress.setDescription("In Progress");

        statusCompleted = new TaskStatus();
        statusCompleted.setCode(TaskStatusEnum.COMPLETED);
        statusCompleted.setDescription("Completed");

        // Save these statuses to the TaskStatus table in the H2 database!
        taskStatusRepository.saveAll(List.of(statusNew, statusPending, statusInProgress, statusCompleted));
    }
}
