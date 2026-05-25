package com.example.taskmanager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class ServerController {

//    @GetMapping
//    public String health() {          // Actuator removes the need for this
//        return "Server is up!";
//    }
}
