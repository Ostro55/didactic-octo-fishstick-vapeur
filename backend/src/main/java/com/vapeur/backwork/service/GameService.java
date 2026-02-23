package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final GameRepository gameRepository;

    @Override
    public List<Game> getAll() {
        return gameRepository.findAll();
    }

    @Override
    public Optional<Game> getById(Long id) {
        return gameRepository.findById(id);
    }

    @Override
    public List<Game> getAllWithFilters(String name, Long minPrice, Long maxPrice, String genre) {
        List<Game> toFilter = gameRepository.findAll();
        var stream = toFilter.stream();
        if (name != null)
            stream = stream.filter(x -> x.getName().equalsIgnoreCase(name));
        if (minPrice != null)
            stream = stream.filter(x -> x.getPrice() >= minPrice);
        if (maxPrice != null)
            stream = stream.filter(x -> x.getPrice() <= maxPrice);
        if (genre != null)
            stream = stream.filter(x -> x.getGenre().contains(genre));
        return stream.toList();
    }

    @Override
    public Optional<Game> addGame(Game newGame) {
        gameRepository.save(newGame);
        return Optional.of(newGame);
    }

    @Override
    public Optional<Game> deleteGameById(Long id) {
        Optional<Game> game = gameRepository.findById(id);
        game.ifPresent(gameRepository::delete);
        return game;
    }
}
