package com.example.taskmanager.repository;

import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Integer> {
    Optional<TaskStatus> findTaskStatusByCode(TaskStatusEnum code);
}
