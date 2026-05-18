package com.example.taskmanager.service;

import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    public TaskStatus getByCode(TaskStatusEnum taskStatusEnum) {
        log.info("Getting task status '{}' from database", taskStatusEnum);

        return taskStatusRepository.findTaskStatusByCode(taskStatusEnum)
                .orElseThrow(() -> new RuntimeException("Status not found"));
    }
}
