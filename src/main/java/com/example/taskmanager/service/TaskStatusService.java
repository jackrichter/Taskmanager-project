package com.example.taskmanager.service;

import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    // Schedules clearing up the cash every 6 hours
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)      // 6 hours in ms
    @CacheEvict(value = "taskStatuses", allEntries = true)
    public void clearTaskStatusCache() {
        log.info("Cache cleared");
    }

    @Cacheable("taskStatuses")
    public TaskStatus getByCode(TaskStatusEnum taskStatusEnum) {
        log.info("Getting task status '{}' from database", taskStatusEnum);

        return taskStatusRepository.findTaskStatusByCode(taskStatusEnum)
                .orElseThrow(() -> new RuntimeException("Status not found"));
    }
}
