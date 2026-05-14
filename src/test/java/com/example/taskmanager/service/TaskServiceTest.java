package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskWithUserDto;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldSaveAndReturnTaskDto() {
        // Given
        TaskDto inputDto = new TaskDto();
        inputDto.setTitle("Learn Spring");

        Task taskEntity = new Task();

        Task savedTask = new Task();
        savedTask.setId(1);

        TaskDto returnedDto = new TaskDto();
        returnedDto.setTitle("Learn Spring");

        // When
        when(modelMapper.map(inputDto, Task.class)).thenReturn(taskEntity);
        when(taskRepository.save(taskEntity)).thenReturn(savedTask);
        when(modelMapper.map(savedTask, TaskDto.class)).thenReturn(returnedDto);

        // Invoke the method under test
        TaskDto result = taskService.createTask(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Learn Spring");
        verify(taskRepository, times(1)).save(taskEntity);
    }

    @Test
    void  createMultipleTasks_shouldSaveAllTasks() {
        // Given
        TaskDto dto1 = new TaskDto();
        TaskDto dto2 = new TaskDto();

        Task task1 = new Task();
        Task task2 = new Task();

        // When
        when(modelMapper.map(dto1, Task.class)).thenReturn(task1);
        when(modelMapper.map(dto2, Task.class)).thenReturn(task2);

        // Invoke the method under test
        taskService.createMultipleTasks(List.of(dto1, dto2));

        // Then
        verify(taskRepository, times(1)).saveAll(List.of(task1, task2));
    }

    @Test
    void getAllTasks_shouldReturnMappedPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id"));

        Task task = new Task();
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        TaskDto dto = new TaskDto();

        // When
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(modelMapper.map(task, TaskDto.class)).thenReturn(dto);

        // Invoke the method under test
        Page<TaskDto> result = taskService.getAllTasks(pageable);

        // Then
        assertThat(1).isEqualTo(result.getContent().size());
        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    void getTaskById_shouldReturnTaskWithUserDto() {
        // Given
        Integer id = 1;

        Task task = new Task();
        task.setId(id);

        TaskWithUserDto dto = new TaskWithUserDto();
        dto.setId(id);

        // When
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(modelMapper.map(task, TaskWithUserDto.class)).thenReturn(dto);

        // Invoke the method under test
        TaskWithUserDto result = taskService.getTaskById(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        verify(taskRepository, times(1)).findById(id);
    }

    @Test
    void getTaskById_shouldThrowException_whenTaskNotFound() {
        // Given
        Integer id = 99;

        // When
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> taskService.getTaskById(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: " + id);

        verify(taskRepository, times(1)).findById(id);
    }

    @Test
    void updateTask_shouldUpdateAndReturnDto() {
        // Given
        Integer id = 1;

        Task existingTask = new Task();
        existingTask.setId(id);

        TaskDto updateDto = new TaskDto();
        updateDto.setTitle("Updated Title");

        Task updatedTask = new Task();
        updatedTask.setId(id);

        TaskDto returnedDto = new TaskDto();
        returnedDto.setTitle("Updated Title");

        // When
        when(taskRepository.findById(id)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(modelMapper.map(updatedTask, TaskDto.class)).thenReturn(returnedDto);

        // Invoke the method under test
        TaskDto result = taskService.updateTask(id, updateDto);

        // Then
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(taskRepository, times(1)).findById(id);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void deleteTask_shouldThrowException_whenTaskNotFound() {
        // Given
        Integer id = 5;

        // When
        when(taskRepository.existsById(id)).thenReturn(false);

        // Then
        assertThatThrownBy(() -> taskService.deleteTask(id))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, times(1)).existsById(id);
    }

    @Test
    void deleteTask_shouldDeleteTaskAndReturnVoid() {
        // Given
        Integer id = 5;

        // When
        when(taskRepository.existsById(id)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(id);            // doNothing() is a Mockito stub for a void method.

        // Invoke the method under test
        taskService.deleteTask(id);

        // Then
        verify(taskRepository, times(1)).existsById(id);
        verify(taskRepository, times(1)).deleteById(id);
    }
}
