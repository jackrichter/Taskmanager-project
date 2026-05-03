package com.example.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithTaskDto {

    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private List<TaskDto> tasks;
}
