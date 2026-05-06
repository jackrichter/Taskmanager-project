package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final @Qualifier("customModelMapper") ModelMapper modelMapper;
    private final @Qualifier("customJsonMapper") ObjectMapper objectMapper;

    public TaskDto createTask (TaskDto taskDto) {

        log.info("Creating new task with title: {}", taskDto.getTitle());

        Task task = modelMapper.map(taskDto, Task.class);
        task.setCreatedAt(LocalDateTime.now());
        Task savesTask = taskRepository.save(task);

//        try {
//            // Convert object to JSON string manually
//            String jsonString = objectMapper.writeValueAsString(savesTask);
//            System.out.println("JSON String: " + jsonString);
//
//            // Convert JSON string back to Task object
//            Task savedTask = objectMapper.readValue(jsonString, Task.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        log.info("Task created successfully with id {}", savesTask.getId());

        return modelMapper.map(savesTask, TaskDto.class);
    }

    public void createMultipleTasks(List<TaskDto> taskDtos) {

        log.info("Creating {} tasks in bulk", taskDtos.size());

        List<Task> tasks = taskDtos.stream()
                .map(taskDto -> modelMapper.map(taskDto, Task.class))
                .toList();

        taskRepository.saveAll(tasks);

        log.info("Bulk task creation completed successfully");
    }

    public Page<TaskDto> getAllTasks(Pageable pageable) {

        log.info("Fetching tasks with page: {}, size: {}, sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<Task> taskPage = taskRepository.findAll(pageable);

        log.info("Fetched {} tasks form database", taskPage.getNumberOfElements());

        return taskPage
                .map(task -> modelMapper.map(task, TaskDto.class));

//        Sort forcedSort = Sort.by("title").descending();
//        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), forcedSort);
//
//        return taskRepository.findAll(newPageable)
//                .map(task -> modelMapper.map(task, TaskDto.class));
    }

    public TaskWithUserDto getTaskById(Integer id) {
        // this code is only for demonstration and doesn't add any value to the code
        // --- Start
//         Task task1 = taskRepository.findById(2).get();
//         TaskDto taskDto = modelMapper.map(task1, TaskDto.class);
//         User user = task1.getUser();
        // --- End

        log.info("Fetching task with id: {}", id);

        return taskRepository.findById(id)
                .map(task -> {
                    log.debug("Task found: {}", task.getId());
                    return modelMapper.map(task, TaskWithUserDto.class);
                })
                .orElseThrow(() -> {
                    log.error("Task with id: {} not found", id);
                    return new TaskNotFoundException("Task not found with id: " + id);
                }); //Because we have an Optional!
    }

    public TaskDto updateTask(Integer id, TaskDto taskDto) {

        log.info("Updating task with id: {}", id);

        Task existingTask = taskRepository.findById(id).orElseThrow(() -> {
            log.error("Cannot update task. Task with id: {} not found", id);
            return new TaskNotFoundException("Task not found with id: " + id);
        });

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());

        Task updatedTask = taskRepository.save(existingTask);

        log.info("Task updated successfully with id: {}", updatedTask.getId());

        return modelMapper.map(updatedTask, TaskDto.class);
    }

    public void deleteTask(Integer id) {

        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            log.error("Task with id: {} not found", id);
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);

        log.info("Task deleted successfully with id: {}", id);
    }
}
