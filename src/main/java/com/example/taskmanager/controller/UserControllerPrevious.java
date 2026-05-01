package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserDto;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserServicePrevious;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserControllerPrevious {

    private final UserServicePrevious userServicePrevious;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> usersDtos = userServicePrevious.getAllUsers();
        return ResponseEntity.ok(usersDtos);
    }

    // GET/user/sorted?page=...&size=...&sort=...,desc/asc Need just one of them for
    // SpringBoot to automatically create a Pageable object!!! (No @RequestBody needed)
    @GetMapping(value = "/sorted")
    public ResponseEntity<Page<UserDto>> getSortedUsers(Pageable pageable) {
        Page<UserDto> usersDtos = userServicePrevious.getSortedUsers(pageable);
        return ResponseEntity.ok(usersDtos);
    }

//    @GetMapping(value = "/sorted")
//    public ResponseEntity<List<UserDto>> getSortedUsers(
//            @RequestParam(defaultValue = "0") int skip,
//            @RequestParam(defaultValue = "5") int limit
//    ) {
//        List<UserDto> usersDtos = userService.getSortedUsers(skip, limit);
//        return ResponseEntity.ok(usersDtos);
//    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        return ResponseEntity.ok(userServicePrevious.checkIfUserExists(email));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<User>> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userServicePrevious.getUserByEmail(email));
    }

    @GetMapping("/fragment/{email}")
    public ResponseEntity<List<User>> getAllByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userServicePrevious.searchByEmailFragment(email));
    }

    @PutMapping("/update/email/{email}/age/{age}")
    public ResponseEntity<Void> updateUserAgeByEmail(@PathVariable String email, @PathVariable Integer age) {
        userServicePrevious.updateUserAgeByEmail(email, age);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Void> addUser(@RequestBody UserDto userDto) {
        userServicePrevious.addUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        userServicePrevious.updateUser(id, userDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userServicePrevious.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
