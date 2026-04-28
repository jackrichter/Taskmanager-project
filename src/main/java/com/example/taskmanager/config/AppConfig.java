package com.example.taskmanager.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper customModelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ObjectMapper customJsonMapper() {
        return new ObjectMapper();
    }
}
