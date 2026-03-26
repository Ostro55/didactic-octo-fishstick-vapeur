package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> addUser(User newUser) {
        userRepository.save(newUser);
        return Optional.of(newUser);
    }

    @Override
    @Transactional
    public Optional<User> updateUser(Long id, User updatedUser) {
        Optional<User> existingOpt = userRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        User existing = existingOpt.get();
        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        existing.setAdmin(updatedUser.isAdmin());
        existing.setRecommendedGames(updatedUser.getRecommendedGames() == null ? new HashSet<>() : updatedUser.getRecommendedGames());
        userRepository.save(existing);
        return Optional.of(existing);
    }

    @Override
    @Transactional
    public Optional<User> deleteUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> {
            // Ensure join table is cleared before deleting the user (works for both H2 and Postgres).
            userRepository.deleteRecommendedGameLinksForUser(u.getId());
            userRepository.delete(u);
        });
        return user;
    }

    @Override
    @Transactional
    public void cleanUsers() {
        // Order matters because of foreign keys.
        userRepository.deleteAllRecommendedGameLinks();
        userRepository.deleteAllUsers();
    }
}
