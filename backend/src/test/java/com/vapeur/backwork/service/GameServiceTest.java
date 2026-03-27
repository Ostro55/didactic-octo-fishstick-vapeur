package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.utils.GameGenre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    GameRepository gameRepository;

    @InjectMocks
    GameService gameService;

    @Test
    void getAll_delegatesToRepository() {
        Game a = new Game();
        a.setName("A");
        Game b = new Game();
        b.setName("B");
        List<Game> games = List.of(a, b);

        when(gameRepository.findAll()).thenReturn(games);

        List<Game> out = gameService.getAll();
        assertSame(games, out);
        verify(gameRepository).findAll();
    }

    @Test
    void getById_delegatesToRepository() {
        Game g = new Game();
        g.setName("Doom");

        when(gameRepository.findById(42L)).thenReturn(Optional.of(g));

        Optional<Game> out = gameService.getById(42L);
        assertTrue(out.isPresent());
        assertSame(g, out.get());
        verify(gameRepository).findById(42L);
    }

    @Test
    void getAllWithFilters_filtersInMemoryAfterFindAll() {
        Game a = new Game();
        a.setName("Doom");
        a.setGenre(Set.of(GameGenre.action));

        Game b = new Game();
        b.setName("Mario");
        b.setGenre(Set.of(GameGenre.romance));

        when(gameRepository.findAll()).thenReturn(List.of(a, b));

        List<Game> out = gameService.getAllWithFilters("doom", null, null, null);
        assertEquals(List.of(a), out);
        verify(gameRepository).findAll();
    }

    @Test
    void addGame_savesAndReturnsSameInstance() {
        Game g = new Game();
        g.setName("Doom");

        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Game> out = gameService.addGame(g);
        assertTrue(out.isPresent());
        assertSame(g, out.get());
        verify(gameRepository).save(g);
    }

    @Test
    void deleteGameById_whenPresent_deletesAndReturnsGame() {
        Game g = new Game();
        g.setName("Doom");

        when(gameRepository.findById(1L)).thenReturn(Optional.of(g));

        Optional<Game> out = gameService.deleteGameById(1L);
        assertTrue(out.isPresent());
        assertSame(g, out.get());
        verify(gameRepository).findById(1L);
        verify(gameRepository).delete(g);
    }

    @Test
    void deleteGameById_whenMissing_doesNotDelete() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Game> out = gameService.deleteGameById(1L);
        assertTrue(out.isEmpty());
        verify(gameRepository).findById(1L);
        verify(gameRepository, never()).delete(any(Game.class));
    }

    @Test
    void cleanGames_callsDeleteQueriesInOrder() {
        gameService.cleanGames();

        InOrder order = inOrder(gameRepository);
        order.verify(gameRepository).deleteAllRecommendedGameLinks();
        order.verify(gameRepository).deleteAllGameGenres();
        order.verify(gameRepository).deleteAllGames();
    }
}

