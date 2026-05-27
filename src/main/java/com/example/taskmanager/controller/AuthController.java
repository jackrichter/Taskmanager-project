package com.example.taskmanager.controller;

import com.example.taskmanager.dto.AuthRequestDto;
import com.example.taskmanager.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequestDto request) {

        authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok("Login successful");
    }
}
