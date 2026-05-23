package com.example.taskmanager.service;

import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskStatusRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
public class TaskStatusServiceTest {

    @MockitoBean
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusService taskStatusService;

    @AfterEach
    void teatDown() {
        taskStatusService.clearTaskStatusCache();
    }

    @Test
    void testGetByCode_taskStatusShouldBeFound() {
        // Given
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.PENDING;

        TaskStatus expectedTaskStatus = new TaskStatus();
        expectedTaskStatus.setCode(taskStatusEnum);
        expectedTaskStatus.setDescription("Pending");

        // When
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(java.util.Optional.of(expectedTaskStatus));

        // Invoke service logic
        TaskStatus result = taskStatusService.getByCode(taskStatusEnum);

        // Then
        assertNotNull(result);
        assertEquals(taskStatusEnum, result.getCode());
        assertEquals("Pending", result.getDescription());
    }

    @Test
    void testGetByCode_taskStatusNotFound() {
        // Given
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.COMPLETED;

        // When
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.empty());

        // Invoke service logic with Exception
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskStatusService.getByCode(taskStatusEnum));

        // Then
        assertEquals("Status not found", exception.getMessage());
    }

    @Test
    void testCacheBehavior() {
        // Given
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.PENDING;

        TaskStatus expectedTaskStatus = new TaskStatus();
        expectedTaskStatus.setCode(taskStatusEnum);
        expectedTaskStatus.setDescription("Pending");

        // Mock the repository for the first call
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(expectedTaskStatus));

        // Call the service to load the status from db (first time)
        TaskStatus result1 = taskStatusService.getByCode(taskStatusEnum);

        // Assert that the status was loaded from db
        assertNotNull(result1);
        assertEquals("Pending", result1.getDescription());

        // Call the service again. This time to load the Status from the cache
        TaskStatus result2 = taskStatusService.getByCode(taskStatusEnum);

        // Verify that the second call didn't hit the database
        verify(taskStatusRepository, times(1)).findTaskStatusByCode(taskStatusEnum);

        // Assert that the same object was returned from memory as the first call
        assertSame(result1, result2);
    }

    @Test
    void testCacheIsClearedAfterEviction() {
        // Given
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.PENDING;

        TaskStatus firstStatus = new TaskStatus();
        firstStatus.setCode(taskStatusEnum);
        firstStatus.setDescription("Pending");

        TaskStatus secondStatus = new TaskStatus();
        secondStatus.setCode(taskStatusEnum);
        secondStatus.setDescription("Pending updated");

        // Mock the first DB call
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(firstStatus));

        // Call DB and load into the cache
        TaskStatus result1 = taskStatusService.getByCode(taskStatusEnum);
        assertEquals("Pending", result1.getDescription());

        // Get the cached object
        TaskStatus cachedResult = taskStatusService.getByCode(taskStatusEnum);
        assertSame(result1, cachedResult);

        // Evict the cache
        taskStatusService.clearTaskStatusCache();

        // Mock the second DB call
        when(taskStatusRepository.findTaskStatusByCode(taskStatusEnum))
                .thenReturn(Optional.of(secondStatus));

        // Call the DB a second time
        TaskStatus resultAfterEviction = taskStatusService.getByCode(taskStatusEnum);
        assertEquals("Pending updated", resultAfterEviction.getDescription());

        // Verify that the repository was called twice
        verify(taskStatusRepository, times(2)).findTaskStatusByCode(taskStatusEnum);
    }
}
