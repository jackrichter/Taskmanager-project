package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserWithTaskDto getUserById(Integer id) {

        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserWithTaskDto.class))
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}
