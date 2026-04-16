package com.vapeur.backwork.service;

import com.vapeur.backwork.RequestDto.UserRequestDto;
import com.vapeur.backwork.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    List<User> getAll();

    Optional<User> getById(Long id);

    Optional<User> addUser(User newUser);

    Optional<User> updateUser(Long id, User updatedUser);

    Optional<User> deleteUserById(Long id);

    Optional<User> login(UserRequestDto userRequestDto);

    /**
     * Purge all data related to users (users + join tables). Useful before schema changes.
     */
    void cleanUsers();
}

