package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.dto.UserDtoPrevious;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.exception.UserNotValidException;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserPrevious;
import com.example.taskmanager.repository.UserRepositoryPrevious;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServicePrevious {

    private final UserRepositoryPrevious userRepositoryPrev;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public List<UserDtoPrevious> getAllUsers() {

        log.info("Get all users");

        List<UserPrevious> users = userRepositoryPrev.findAll();

        return users.stream()
                .map(user -> modelMapper.map(user, UserDtoPrevious.class))
                .toList();
    }

//    public List<UserDtoPrevious> getAllUsers() {
//
//        List<UserPrevious> users = userRepositoryPrev.findAll();
//
//        List<UserDtoPrevious> userDtos = new ArrayList<>();
//
//        for (UserPrevious user : users) {
//            userDtos.add(modelMapper.map(user, UserDtoPrevious.class));
//        }
//
//        return userDtos;
//    }

    // Very Important!!! Using pagination with sorting
    public Page<UserDtoPrevious> getSortedUsers(Pageable pageable) {

        log.info("Get sorted users");

        Sort sort = Sort.by("name").ascending()
                .and(Sort.by("email").descending());

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // With Pageable we don't need .stream()!!! (It's an exception to the rule)
        return userRepositoryPrev.findAll(sortedPageable)
                .map(user -> modelMapper.map(user, UserDtoPrevious.class));
    }

    // Manually sorting and achieving pagination
    public List<UserDtoPrevious> getManuallySortedUsers(int skip, int limit) {

        List<UserPrevious> users = userRepositoryPrev.findAll();

        // ATTENTION! This approach works only AFTER all data has been loaded and in memory! Not efficient for pagination of big datasets!
        // Sorting and paging results with streams. Sort by name. If names are equal, sort by email.
        return users.stream()
                .map(user -> modelMapper.map(user, UserDtoPrevious.class))
                .sorted(Comparator.comparing(UserDtoPrevious::getName).thenComparing(UserDtoPrevious::getEmail))
                .skip(skip)
                .limit(limit)
                .toList();
    }

    public UserDtoPrevious getUserById(Integer id) {

        log.info("Fetching user with id: {}", id);

        UserPrevious user = userRepositoryPrev.findById(id).orElseThrow(() -> {
            log.error("User with id {} not found", id);
            return new UserNotFoundException("User not found with id: " + id);
        });
        log.debug("Fetched user: {}", user);    // Not in production!

        return modelMapper.map(user, UserDtoPrevious.class);
    }

    public void addUser(UserDtoPrevious userDto) {

        log.info("Adding user {}", userDto.getName());

        if (userDto == null || userDto.getName() == null || userDto.getName().isBlank() || userDto.getAge() < 0) {
            throw new UserNotValidException();
        }
        UserPrevious user = modelMapper.map(userDto, UserPrevious.class);
        userRepositoryPrev.save(user);
    }

    public void updateUser(Integer index, UserDtoPrevious userDto) {

        log.info("Updating user {}", userDto.getName());

        if (userRepositoryPrev.existsById(index)) {
            userRepositoryPrev.save(modelMapper.map(userDto, UserPrevious.class));
        } else {
            throw new UserNotFoundException("User not found with id: " + index);
        }
    }

    public void deleteUser(Integer index) {

        log.info("Deleting user {}", index);

        if (userRepositoryPrev.existsById(index)) {
            userRepositoryPrev.deleteById(index);
        } else {
            throw new UserNotFoundException("User not found with id: " + index);
        }
    }

    public boolean checkIfUserExists(String email) {
        boolean exists = userRepositoryPrev.existsUserByEmail(email);

        if (!exists) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return true;
    }

    public List<UserPrevious> getUserByEmail(String email) {
        return userRepositoryPrev.getUserByEmail(email);
    }

    public List<UserPrevious> searchByEmailFragment(String email) {
        return userRepositoryPrev.searchByEmailFragment(email);
    }

    @Transactional
    public void updateUserAgeByEmail(String email, Integer age) {
        userRepositoryPrev.updateUserAgeByEmail(email, age);
    }
}
