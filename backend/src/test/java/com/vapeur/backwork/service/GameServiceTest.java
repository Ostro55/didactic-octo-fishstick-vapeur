package com.vapeur.backwork.service;

import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    GameRepository gameRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    AuditEventPublisher auditEventPublisher;

    @InjectMocks
    GameService gameService;

    @Test
    void addGame_withAdminUser_setsAccepted() {
        User admin = user(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        Game newGame = game("Zelda");
        Optional<Game> created = gameService.addGame(newGame, 1L);

        assertTrue(created.isPresent());
        assertEquals("accepted", created.get().getStatus());

        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        assertEquals("accepted", captor.getValue().getStatus());
        verify(auditEventPublisher).publish(any());
    }

    @Test
    void addGame_withNonAdminUser_setsPending() {
        User user = user(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Game newGame = game("Hades");
        Optional<Game> created = gameService.addGame(newGame, 2L);

        assertTrue(created.isPresent());
        assertEquals("pending", created.get().getStatus());

        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        assertEquals("pending", captor.getValue().getStatus());
        verify(auditEventPublisher).publish(any());
    }

    @Test
    void addGame_withUnknownUser_returnsEmptyAndDoesNotSave() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Game newGame = game("Celeste");
        Optional<Game> created = gameService.addGame(newGame, 999L);

        assertTrue(created.isEmpty());
        verify(gameRepository, never()).save(any());
        verify(auditEventPublisher, never()).publish(any());
    }

    private static User user(boolean isAdmin) {
        User u = new User();
        u.setUsername("u");
        u.setPassword("p");
        u.setEmail("u@example.com");
        u.setAdmin(isAdmin);
        return u;
    }

    private static Game game(String name) {
        Game g = new Game();
        g.setName(name);
        g.setPrice(10L);
        return g;
    }
}
