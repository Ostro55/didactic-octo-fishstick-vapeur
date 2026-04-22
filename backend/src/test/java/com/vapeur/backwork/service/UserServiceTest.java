package com.vapeur.backwork.service;

import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AuditEventPublisher auditEventPublisher;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void addUser_savesAndReturnsSameInstance() {
        User u = new User();
        u.setUsername("alice");
        u.setPassword("pw");
        u.setEmail("alice@example.com");
        u.setAdmin(false);

        when(passwordEncoder.encode("pw")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<User> out = userService.addUser(u);
        assertTrue(out.isPresent());
        assertSame(u, out.get());
        assertEquals("hashed-pw", u.getPassword());
        verify(passwordEncoder).encode("pw");
        verify(userRepository).save(u);
        verify(auditEventPublisher).publish(any());
    }

    @Test
    void updateUser_whenMissing_returnsEmptyAndDoesNotSave() {
        User updated = new User();
        updated.setUsername("bob");
        updated.setPassword("pw");
        updated.setEmail("bob@example.com");
        updated.setAdmin(true);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> out = userService.updateUser(99L, updated);
        assertTrue(out.isEmpty());
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
        verify(auditEventPublisher, never()).publish(any());
    }

    @Test
    void updateUser_whenPresent_updatesFields_hashesPassword_andNullRecommendedGamesBecomesEmptySet() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setPassword("old-hash");
        existing.setEmail("old@example.com");
        existing.setAdmin(false);
        existing.setRecommendedGames(Set.of());

        User updated = new User();
        updated.setUsername("new");
        updated.setPassword("ignored"); // service doesn't touch password currently
        updated.setEmail("new@example.com");
        updated.setAdmin(true);
        updated.setRecommendedGames(null);

        when(passwordEncoder.encode("ignored")).thenReturn("new-hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<User> out = userService.updateUser(1L, updated);
        assertTrue(out.isPresent());
        assertSame(existing, out.get());
        assertEquals("new", existing.getUsername());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals("new-hash", existing.getPassword());
        assertTrue(existing.isAdmin());
        assertNotNull(existing.getRecommendedGames());
        assertTrue(existing.getRecommendedGames().isEmpty());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("ignored");
        verify(userRepository).save(existing);
        verify(auditEventPublisher).publish(any());
    }

    @Test
    void updateUser_whenPresent_preservesProvidedRecommendedGamesInstance() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("old");
        existing.setPassword("pw");
        existing.setEmail("old@example.com");
        existing.setAdmin(false);

        Game g = new Game();
        g.setName("Doom");
        Set<Game> rec = new HashSet<>();
        rec.add(g);

        User updated = new User();
        updated.setUsername("new");
        updated.setPassword("pw");
        updated.setEmail("new@example.com");
        updated.setAdmin(true);
        updated.setRecommendedGames(rec);

        when(passwordEncoder.encode("pw")).thenReturn("hashed-pw");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUser(1L, updated);
        assertSame(rec, existing.getRecommendedGames());
        assertEquals("hashed-pw", existing.getPassword());
    }

    @Test
    void deleteUserById_whenPresent_clearsJoinTableThenDeletes() {
        User u = new User();
        u.setId(5L);
        u.setUsername("alice");
        u.setPassword("pw");
        u.setEmail("alice@example.com");
        u.setAdmin(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        Optional<User> out = userService.deleteUserById(5L);
        assertTrue(out.isPresent());
        assertSame(u, out.get());

        InOrder order = inOrder(userRepository);
        order.verify(userRepository).findById(5L);
        order.verify(userRepository).deleteRecommendedGameLinksForUser(5L);
        order.verify(userRepository).delete(u);
        verify(auditEventPublisher).publish(any());
    }

    @Test
    void deleteUserById_whenMissing_doesNotDelete() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        Optional<User> out = userService.deleteUserById(5L);
        assertTrue(out.isEmpty());
        verify(userRepository).findById(5L);
        verify(userRepository, never()).deleteRecommendedGameLinksForUser(any(Long.class));
        verify(userRepository, never()).delete(any(User.class));
        verify(auditEventPublisher, never()).publish(any());
    }

    @Test
    void cleanUsers_callsDeleteQueriesInOrder() {
        userService.cleanUsers();

        InOrder order = inOrder(userRepository);
        order.verify(userRepository).deleteAllRecommendedGameLinks();
        order.verify(userRepository).deleteAllUsers();
        verify(auditEventPublisher).publish(any());
    }

    @Test
    void login_withMatchingHash_returnsUser() {
        User existing = new User();
        existing.setEmail("alice@example.com");
        existing.setPassword("hashed-pw");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("pw", "hashed-pw")).thenReturn(true);

        Optional<User> out = userService.login(new com.vapeur.backwork.RequestDto.UserRequestDto("alice@example.com", "pw"));

        assertTrue(out.isPresent());
        assertSame(existing, out.get());
        verify(userRepository).findByEmail("alice@example.com");
        verify(passwordEncoder).matches("pw", "hashed-pw");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withLegacyPlaintextPassword_migratesToHash() {
        User existing = new User();
        existing.setEmail("alice@example.com");
        existing.setPassword("pw");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("pw", "pw")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<User> out = userService.login(new com.vapeur.backwork.RequestDto.UserRequestDto("alice@example.com", "pw"));

        assertTrue(out.isPresent());
        assertEquals("hashed-pw", existing.getPassword());
        verify(passwordEncoder).matches("pw", "pw");
        verify(passwordEncoder).encode("pw");
        verify(userRepository).save(existing);
    }

    @Test
    void login_withWrongPassword_returnsEmpty() {
        User existing = new User();
        existing.setEmail("alice@example.com");
        existing.setPassword("hashed-pw");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "hashed-pw")).thenReturn(false);

        Optional<User> out = userService.login(new com.vapeur.backwork.RequestDto.UserRequestDto("alice@example.com", "wrong"));

        assertTrue(out.isEmpty());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
