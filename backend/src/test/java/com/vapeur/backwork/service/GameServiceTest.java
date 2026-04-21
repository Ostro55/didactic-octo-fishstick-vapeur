package com.vapeur.backwork.service;

import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.repository.UserRepository;
import com.vapeur.backwork.utils.GameGenre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Test
    void importGamesFromCsv_withAdmin_importsAllGamesAsProvided() {
        User admin = user(true);
        admin.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            if (game.getId() == null) {
                game.setId(100L);
            }
            return game;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "games.csv",
                "text/csv",
                (
                        "name,price,description,release_date,img_url,editor,genre,status\n" +
                        "\"Doom\",1999,\"FPS\",2024-01-15,https://example.com/doom.jpg,id,action|horror,accepted\n" +
                        "\"It Takes Two\",3999,\"Co-op\",2021-03-26T12:30:00,https://example.com/itt.jpg,ea,multiplayer|action,pending\n"
                ).getBytes(StandardCharsets.UTF_8)
        );

        List<Game> imported = gameService.importGamesFromCsv(file, 1L);

        assertEquals(2, imported.size());
        assertEquals("Doom", imported.get(0).getName());
        assertEquals(Set.of(GameGenre.action, GameGenre.horror), imported.get(0).getGenre());
        assertEquals(Timestamp.valueOf(LocalDateTime.of(2024, 1, 15, 0, 0)), imported.get(0).getRelease_date());
        assertEquals("pending", imported.get(1).getStatus());
        verify(gameRepository, times(2)).save(any(Game.class));
        verify(auditEventPublisher, times(2)).publish(any());
    }

    @Test
    void importGamesFromCsv_withNonAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(false)));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "games.csv",
                "text/csv",
                "name,price,description,release_date,img_url,editor,genre,status\n".getBytes(StandardCharsets.UTF_8)
        );

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> gameService.importGamesFromCsv(file, 2L));

        assertEquals("L'utilisateur doit etre admin pour importer des jeux.", thrown.getMessage());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void importGamesFromCsv_withInvalidGenre_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(true)));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "games.csv",
                "text/csv",
                (
                        "name,price,description,release_date,img_url,editor,genre,status\n" +
                        "\"Doom\",1999,\"FPS\",2024-01-15,https://example.com/doom.jpg,id,invalid,accepted\n"
                ).getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> gameService.importGamesFromCsv(file, 1L));

        assertTrue(thrown.getMessage().startsWith("Genre invalide:"));
        verify(gameRepository, never()).save(any());
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
