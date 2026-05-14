package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserWithTaskDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        Integer userId = 1;

        // Given
        User user = new User();
        user.setId(userId);

        UserWithTaskDto dto = new UserWithTaskDto();

        // When
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserWithTaskDto.class)).thenReturn(dto);

        // Invoke the method under test
        UserWithTaskDto result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {

        // Given
        Integer userId = 99;

        // When
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userId);
    }
}
