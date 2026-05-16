package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatusEnum;
import com.example.taskmanager.model.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    private Integer id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private TaskStatusEnum status;
}
