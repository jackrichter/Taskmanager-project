package com.example.taskmanager.dto;

import com.example.taskmanager.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Integer id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private TaskStatusEnum status;
}
