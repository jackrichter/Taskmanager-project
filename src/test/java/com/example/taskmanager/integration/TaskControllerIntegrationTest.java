package com.example.taskmanager.integration;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.enums.TaskStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                .file(file))
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
                .file(file))
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
                .file(file))
                .andExpect(status().isBadRequest());
    }

    /* -------------------------------------------------
       GET /tasks (Pageable)
     ------------------------------------------------- */
    @Test
    void getAllTasks_shouldReturnPagedResult() throws Exception {
        // Then
        mockMvc.perform(get("/tasks")
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
        mockMvc.perform(get("/tasks/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void getTaskById_shouldReturn404_whenTaskNotFound() throws Exception {
        // Then
        mockMvc.perform(get("/tasks/{id}", 9999))
                .andExpect(status().isNotFound());
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
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void updateTask_shouldFailOnValidation() throws Exception {
        // Then
        mockMvc.perform(put("/tasks/{id}", 1)
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
        mockMvc.perform(delete("/tasks/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    /* -------------------------------------------------
       Create a Task
     ------------------------------------------------- */
    private String createTask(TaskDto dto) throws Exception {
        return mockMvc.perform(post("/tasks")
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
}
