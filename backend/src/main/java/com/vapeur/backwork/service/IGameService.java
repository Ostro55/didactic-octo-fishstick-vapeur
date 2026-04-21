package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;

import java.util.List;
import java.util.Optional;

public interface IGameService {

    List<Game> getAll();

    Optional<Game> getById(Long id);

    List<Game> getAllWithFilters(String name, Long minPrice, Long maxPrice, String genre);

    Optional<Game> addGame(Game newGame);

    /**
     * Create a game with a status derived from the calling user.
     * Admin users accept immediately, others go to pending.
     */
    Optional<Game> addGame(Game newGame, Long userId);

    Optional<Game> deleteGameById(Long id);

    /**
     * Purge all data related to games (games + join tables). Useful before schema changes.
     */
    void cleanGames();

    Optional<Game> acceptGame(Long id);

    Optional<Game> rejectGame(Long id);
}
