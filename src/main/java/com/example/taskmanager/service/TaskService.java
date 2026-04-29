package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final @Qualifier("customModelMapper") ModelMapper modelMapper;
    private final @Qualifier("customJsonMapper") ObjectMapper objectMapper;

    public TaskDto createTask (TaskDto taskDto) {
        Task task = modelMapper.map(taskDto, Task.class);
        task.setCreatedAt(LocalDateTime.now());
        Task savesTask = taskRepository.save(task);

        try {
            // Convert object to JSON string manually
            String jsonString = objectMapper.writeValueAsString(savesTask);
            System.out.println("JSON String: " + jsonString);

            // Convert JSON string back to Task object
            Task savedTask = objectMapper.readValue(jsonString, Task.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return modelMapper.map(savesTask, TaskDto.class);
    }

    public void createMultipleTasks(List<TaskDto> taskDtos) {
        List<Task> tasks = taskDtos.stream()
                .map(taskDto -> modelMapper.map(taskDto, Task.class))
                .toList();

        taskRepository.saveAll(tasks);
    }

    public List<TaskDto> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(task -> modelMapper.map(task, TaskDto.class))
                .toList();
    }

    public TaskDto getTaskById(Integer id) {

        return taskRepository.findById(id)
                .map(task -> modelMapper.map(task, TaskDto.class))
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id)); //Because we have an Optional!
    }

    public TaskDto updateTask(Integer id, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(id).orElseThrow(
                () -> new TaskNotFoundException("Task not found with id: " + id));

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());

        Task updatedTask = taskRepository.save(existingTask);

        return modelMapper.map(updatedTask, TaskDto.class);
    }

    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}
