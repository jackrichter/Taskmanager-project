package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.exception.UserNotValidException;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<UserDto> getAllUsers() {

        List<User> users = userRepository.findAll();

        List<UserDto> userDtos = new ArrayList<>();

        for (User user : users) {
            userDtos.add(modelMapper.map(user, UserDto.class));
        }

        return userDtos;
    }

    public List<UserDto> getSortedUsers(int skip, int limit) {

        List<User> users = userRepository.findAll();

        // ATTENTION! This approach works only AFTER all data has been loaded and in memory! Not efficient for pagination of big datasets!
        // Sorting and paging results with streams. Sort by name. If names are equal, sort by email.
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .sorted(Comparator.comparing(UserDto::getName).thenComparing(UserDto::getEmail))
                .skip(skip)
                .limit(limit)
                .toList();
    }

    public void addUser(UserDto userDto) {
        if (userDto == null || userDto.getName() == null || userDto.getName().isBlank() || userDto.getAge() < 0) {
            throw new UserNotValidException();
        }
        User user = modelMapper.map(userDto, User.class);
        userRepository.save(user);
    }

    public void updateUser(Long index, UserDto userDto) {
        if (userRepository.existsById(index)) {
            userRepository.save(modelMapper.map(userDto, User.class));
        } else {
            throw new UserNotFoundException(index);
        }
    }

    public void deleteUser(Long index) {
        if (userRepository.existsById(index)) {
            userRepository.deleteById(index);
        } else {
            throw new UserNotFoundException(index);
        }
    }

    public boolean checkIfUserExists(String email) {
        return userRepository.existsUserByEmail(email);
    }

    public List<User> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public List<User> searchByEmailFragment(String email) {
        return userRepository.searchByEmailFragment(email);
    }

    @Transactional
    public void updateUserAgeByEmail(String email, Integer age) {
        userRepository.updateUserAgeByEmail(email, age);
    }
}
