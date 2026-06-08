package com.example.taskmanager.service;

import com.example.taskmanager.dto.AuthRequestDto;
import com.example.taskmanager.enums.RoleEnum;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    // -------- LOGIN TESTS --------

    @Test
    void login_successful() {
        String email = "test@example.com";
        String rawPassword = "password";
        String hashedPassword = "hashedPassword";
        String token = "jwtToken";

        // Given
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(RoleEnum.USER);

        // When
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email, user.getRole())).thenReturn(token);

        // Invoke service logic
        String result = authService.login(email, rawPassword);

        // Then
        assertEquals(token, result);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(jwtUtil).generateToken(email, RoleEnum.USER);
    }

    @Test
    void login_InvalidEmail_ThrowsException() {
        // When
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        // Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login("wrong@example.com", "password"));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        String email = "test@example.com";
        String rawPassword = "rawPassword";
        String hashedPassword = "hashedPassword";

        // Given
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);

        // When
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        // Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(email, rawPassword));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    // -------- SIGNUP TESTS --------

    @Test
    void signup_Successful() {
        // Given
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("new@example.com");
        requestDto.setPassword("password");
        requestDto.setUsername("newUser");

        // When
        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("hashedPassword");

        // Invoke service logic
        authService.signup(requestDto);

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_UserAlreadyExists_ThrowsException() {
        // Given
        AuthRequestDto requestDto = new AuthRequestDto();
        requestDto.setEmail("existing@example.com");

        //When
        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(new User()));

        // Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(requestDto));

        assertEquals("User already exists", exception.getMessage());
    }
}
