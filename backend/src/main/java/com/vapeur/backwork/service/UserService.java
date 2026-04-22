package com.vapeur.backwork.service;

import com.vapeur.backwork.RequestDto.UserRequestDto;
import com.vapeur.backwork.audit.AuditAction;
import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.audit.AuditEvents;
import com.vapeur.backwork.audit.AuditResourceType;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final AuditEventPublisher auditEventPublisher;
    private final PasswordEncoder passwordEncoder;

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
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(newUser);
        auditEventPublisher.publish(AuditEvents.system(
                AuditAction.USER_CREATED,
                AuditResourceType.USER,
                newUser.getId() == null ? null : newUser.getId().toString(),
                Map.of("username", newUser.getUsername())
        ));
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
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existing.setRecommendedGames(updatedUser.getRecommendedGames() == null ? new HashSet<>() : updatedUser.getRecommendedGames());
        userRepository.save(existing);
        auditEventPublisher.publish(AuditEvents.system(
                AuditAction.USER_UPDATED,
                AuditResourceType.USER,
                existing.getId() == null ? null : existing.getId().toString(),
                Map.of(
                        "username", existing.getUsername(),
                        "email", existing.getEmail(),
                        "isAdmin", existing.isAdmin()
                )
        ));
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
            auditEventPublisher.publish(AuditEvents.system(
                    AuditAction.USER_DELETED,
                    AuditResourceType.USER,
                    u.getId() == null ? null : u.getId().toString(),
                    Map.of("username", u.getUsername())
            ));
        });
        return user;
    }

    @Override
    @Transactional
    public Optional<User> login(UserRequestDto userRequestDto) {
        Optional<User> userOpt = userRepository.findByEmail(userRequestDto.email());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        String storedPassword = user.getPassword();
        String rawPassword = userRequestDto.password();

        if (storedPassword != null && passwordEncoder.matches(rawPassword, storedPassword)) {
            return Optional.of(user);
        }

        if (storedPassword != null && storedPassword.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public void cleanUsers() {
        // Order matters because of foreign keys.
        userRepository.deleteAllRecommendedGameLinks();
        userRepository.deleteAllUsers();
        auditEventPublisher.publish(AuditEvents.system(
                AuditAction.USERS_CLEANED,
                AuditResourceType.SYSTEM,
                null,
                Map.of()
        ));
    }
}
