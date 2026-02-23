package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;

import java.util.List;
import java.util.Optional;

public interface IGameService {

    List<Game> getAll();

    Optional<Game> getById(Long id);

    List<Game> getAllWithFilters(List<String> filters);

    Optional<Game> addGame(Game newGame);

    Optional<Game> deleteGameById(Long id);
}
