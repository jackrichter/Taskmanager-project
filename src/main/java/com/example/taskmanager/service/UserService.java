package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserWithTaskDto getUserById(Integer id) {

        log.info("Getting user with id: {}", id);

        return userRepository.findById(id)
                .map(user -> {
                    log.debug("User with id {} found", id);
                    return modelMapper.map(user, UserWithTaskDto.class);
                })
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
    }
}
