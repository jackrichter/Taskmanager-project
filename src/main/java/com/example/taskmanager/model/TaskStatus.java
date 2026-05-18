package com.example.taskmanager.model;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatusEnum code;

    @Column(name = "description", nullable = false)
    private String description;
}
