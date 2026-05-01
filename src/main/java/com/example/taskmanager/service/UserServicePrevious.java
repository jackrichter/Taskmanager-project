package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.exception.UserNotFoundException;
import com.example.taskmanager.exception.UserNotValidException;
import com.example.taskmanager.model.User;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServicePrevious {

    private final UserRepositoryPrevious userRepositoryPrev;
    private final ModelMapper modelMapper;

    public List<UserDto> getAllUsers() {

        List<User> users = userRepositoryPrev.findAll();

        List<UserDto> userDtos = new ArrayList<>();

        for (User user : users) {
            userDtos.add(modelMapper.map(user, UserDto.class));
        }

        return userDtos;
    }

    // Very Important!!! Using pagination with sorting
    public Page<UserDto> getSortedUsers(Pageable pageable) {

        Sort sort = Sort.by("name").ascending()
                .and(Sort.by("email").descending());

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // With Pageable we don't need .stream()!!! (It's an exception to the rule)
        return userRepositoryPrev.findAll(sortedPageable)
                .map(user -> modelMapper.map(user, UserDto.class));
    }

    // Manually sorting and achieving pagination
    public List<UserDto> getManuallySortedUsers(int skip, int limit) {

        List<User> users = userRepositoryPrev.findAll();

        // ATTENTION! This approach works only AFTER all data has been loaded and in memory! Not efficient for pagination of big datasets!
        // Sorting and paging results with streams. Sort by name. If names are equal, sort by email.
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .sorted(Comparator.comparing(UserDto::getName).thenComparing(UserDto::getEmail))
                .skip(skip)
                .limit(limit)
                .toList();
    }

    public UserDto getUserById(Long id) {

        log.info("Fetching user with id: {}", id);

        User user = userRepositoryPrev.findById(id).orElseThrow(() -> {
            log.error("User with id {} not found", id);
            return new UserNotFoundException(id);
        });
        log.debug("Fetched user: {}", user);    // Not in production!

        return modelMapper.map(user, UserDto.class);
    }

    public void addUser(UserDto userDto) {
        if (userDto == null || userDto.getName() == null || userDto.getName().isBlank() || userDto.getAge() < 0) {
            throw new UserNotValidException();
        }
        User user = modelMapper.map(userDto, User.class);
        userRepositoryPrev.save(user);
    }

    public void updateUser(Long index, UserDto userDto) {
        if (userRepositoryPrev.existsById(index)) {
            userRepositoryPrev.save(modelMapper.map(userDto, User.class));
        } else {
            throw new UserNotFoundException(index);
        }
    }

    public void deleteUser(Long index) {
        if (userRepositoryPrev.existsById(index)) {
            userRepositoryPrev.deleteById(index);
        } else {
            throw new UserNotFoundException(index);
        }
    }

    public boolean checkIfUserExists(String email) {
        return userRepositoryPrev.existsUserByEmail(email);
    }

    public List<User> getUserByEmail(String email) {
        return userRepositoryPrev.getUserByEmail(email);
    }

    public List<User> searchByEmailFragment(String email) {
        return userRepositoryPrev.searchByEmailFragment(email);
    }

    @Transactional
    public void updateUserAgeByEmail(String email, Integer age) {
        userRepositoryPrev.updateUserAgeByEmail(email, age);
    }
}
