package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDtoPrevious;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.exception.UserNotValidException;
import com.example.taskmanager.model.UserPrevious;
import com.example.taskmanager.repository.UserRepositoryPrevious;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServicePreviousTest {

    @Mock
    private UserRepositoryPrevious userRepositoryPrev;

    @InjectMocks
    private UserServicePrevious userServicePrev;

    private ModelMapper modelMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        modelMapper = new ModelMapper();
        objectMapper = new ObjectMapper();
        userServicePrev = new UserServicePrevious(
                userRepositoryPrev,
                modelMapper,
                objectMapper);
    }

    // ----------------------------
    // getAllUsers
    // ----------------------------

    @Test
    void getAllUsers_shouldReturnAllUsersDtos() {
        // Check first that everything is working
//        assert true;

        // Mock som data
        UserPrevious user1 = new UserPrevious();
        user1.setName("Adam");
        user1.setEmail("adam@test.com");

        UserPrevious user2 = new UserPrevious();
        user2.setName("Brian");
        user2.setEmail("brian@test.com");

        // The mocked logic condition for fetching the fake test data
        when(userRepositoryPrev.findAll()).thenReturn(List.of(user1, user2));

        // Test the function we want to call inside our userService
        List<UserDtoPrevious> result = userServicePrev.getAllUsers();

        // Check the result
        assertEquals(2, result.size());

        // Verify that a specific function was called and called exactly x-number of times
        verify(userRepositoryPrev, times(1)).findAll();
    }

    // ----------------------------
    // addUsers
    // ----------------------------

    @Test
    void addUser_shouldThrowException_whenUserIsInvalid() {
        // Mock bad data
        UserDtoPrevious userDto = new UserDtoPrevious();
        userDto.setName("");
        userDto.setAge(-1);

        // Assert that an exception is thrown
        assertThrows(UserNotValidException.class,
                () -> userServicePrev.addUser(userDto));

        // Verify that the save method was never called
        verify(userRepositoryPrev, never()).save(any());
    }

    // ----------------------------
    // updateUsers
    // ----------------------------

    @Test
    void updateUser_shouldSaveUser_whenUserExists() {
        // Given
        UserDtoPrevious userDto = new UserDtoPrevious();
        userDto.setName("updatedName");

        // When
        when(userRepositoryPrev.existsById(1)).thenReturn(true);
        userServicePrev.updateUser(1, userDto);

        // Then
        verify(userRepositoryPrev, times(1)).save(any(UserPrevious.class));
    }

    @Test
    void updateUser_shouldThrowException_wheUserDoesNotExist() {
        // Given
        UserDtoPrevious userDto = new UserDtoPrevious();

        // When
        when(userRepositoryPrev.existsById(1)).thenReturn(false);

        // Then
        assertThrows(UserNotFoundException.class, () -> userServicePrev.updateUser(1, userDto));
    }

    // ----------------------------
    // deleteUsers
    // ----------------------------

    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        // Given is not necessary here

        // When
        when((userRepositoryPrev.existsById(1))).thenReturn(true);
        userServicePrev.deleteUser(1);

        // Then
        verify(userRepositoryPrev, times(1)).deleteById(1);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserDoesNotExist() {
        when(userRepositoryPrev.existsById(1)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userServicePrev.deleteUser(1));
    }

    // ------------------------------
    // checkIfUserExists - By Email
    // ------------------------------

    @Test
    void checkIfUserExists_shouldReturnTrue_whenEmailExists() {
        // When
        when(userRepositoryPrev.existsUserByEmail("test@test.com")).thenReturn(true);
        boolean result = userRepositoryPrev.existsUserByEmail("test@test.com");

        // Then
        assertTrue(result);
    }

    @Test
    void checkIfUserExists_shouldThrowException_whenEmailWasNotFound() {
        // When
        when(userRepositoryPrev.existsUserByEmail("example@example.com")).thenReturn(false);
        boolean result = userRepositoryPrev.existsUserByEmail("example@example.com");

        // Then
        assertThrows(UserNotFoundException.class, () -> userServicePrev.checkIfUserExists("example@example.com"));
    }
}
