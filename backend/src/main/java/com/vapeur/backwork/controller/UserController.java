package com.vapeur.backwork.controller;

import com.vapeur.backwork.RequestDto.UserRequestDto;
import com.vapeur.backwork.RequestDto.UserResponseDto;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("users")
    public ResponseEntity<List<UserResponseDto>> getAll() {
        List<UserResponseDto> allUsers = userService.getAll().stream()
                .map(UserResponseDto::from)
                .toList();
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    @PostMapping("users")
    public ResponseEntity<UserResponseDto> save(@RequestBody User newUser) {
        Optional<User> user = userService.addUser(newUser);
        return user.map(value -> new ResponseEntity<>(UserResponseDto.from(value), HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @GetMapping("users/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable("id") Long id) {
        Optional<User> user = userService.getById(id);
        return user.map(value -> new ResponseEntity<>(UserResponseDto.from(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("users/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable("id") Long id, @RequestBody User updatedUser) {
        Optional<User> user = userService.updateUser(id, updatedUser);
        return user.map(value -> new ResponseEntity<>(UserResponseDto.from(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("users/{id}")
    public ResponseEntity<UserResponseDto> deleteById(@PathVariable("id") Long id) {
        Optional<User> user = userService.deleteUserById(id);
        return user.map(value -> new ResponseEntity<>(UserResponseDto.from(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("users")
    public ResponseEntity<Void> clean() {
        userService.cleanUsers();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("users/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserRequestDto userRequestDto) {
        // This is a plain credential lookup, not a token/session-based authentication flow.
        Optional<User> user = userService.login(userRequestDto);

        return user.map(value -> new ResponseEntity<>(UserResponseDto.from(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
