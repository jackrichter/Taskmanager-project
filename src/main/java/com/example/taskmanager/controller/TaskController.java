package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.exception.InvalidFieldFormatException;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(taskService.createTask(taskDto), HttpStatus.CREATED);
    }

    // File upload through endpoint.
    // MultipartFile represents an uploaded file in a multipart/form-data HTTP request
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadTasks(@RequestParam("file") MultipartFile file) {
        try {
            String json = new String(file.getBytes());

            List<TaskDto> tasks = Arrays.asList(objectMapper.readValue(json, TaskDto[].class));

            // Very Important!!
            // Handling all violations in TaskDto list with streams and only getting the messages themselves.
            List<String> validationMessages = tasks.stream()
                    .flatMap(dto -> validator.validate(dto).stream())
//                    .map(ConstraintViolation::getMessage)
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .toList();

            if (!validationMessages.isEmpty()) {
                return ResponseEntity.badRequest().body(String.join("\n", validationMessages));
            }

            // Handling violations the old way. Catches and reports only the first found violation. Very Important!!
//            for (TaskDto dto : tasks) {
//                Set<ConstraintViolation<TaskDto>> violations = validator.validate(dto);
//                if (!violations.isEmpty()) {
//                    return ResponseEntity.badRequest().body(violations.toString());
//                }
//            }
//
//            // Similarly with streams
//            Optional<Set<ConstraintViolation<TaskDto>>> firstViolation = tasks.stream()
//                            .map(dto -> validator.validate(dto))
//                            .filter(violations -> !violations.isEmpty())
//                            .findFirst();       // Returns an Optional
//            if (firstViolation.isPresent()) {
//                return ResponseEntity.badRequest().body(firstViolation.get().toString());
//            }

            // Service
            taskService.createMultipleTasks(tasks);

            return ResponseEntity.ok("uploaded: " + tasks.size() + " tasks");

        } catch (Exception e) {
            throw new InvalidFieldFormatException("Invalid file format");
        }
    }

    @GetMapping
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @PageableDefault(page = 0, size = 5, sort = "title") Pageable pageable) {

        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Integer id,
            @Valid @RequestBody TaskDto taskDto) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
