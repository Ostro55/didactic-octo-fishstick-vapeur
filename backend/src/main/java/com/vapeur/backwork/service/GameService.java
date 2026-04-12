package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

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
        return GameFilters.apply(toFilter, name, minPrice, maxPrice, genre);
    }

    @Override
    public Optional<Game> addGame(Game newGame) {
        gameRepository.save(newGame);
        return Optional.of(newGame);
    }

    @Override
    public Optional<Game> addGame(Game newGame, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Optional.empty();

        // Never trust client-provided status: derive it from the calling user.
        newGame.setStatus(userOpt.get().isAdmin() ? "accepted" : "pending");
        gameRepository.save(newGame);
        return Optional.of(newGame);
    }

    @Override
    public Optional<Game> deleteGameById(Long id) {
        Optional<Game> game = gameRepository.findById(id);
        game.ifPresent(gameRepository::delete);
        return game;
    }

    @Override
    @Transactional
    public void cleanGames() {
        // Order matters because of foreign keys.
        gameRepository.deleteAllRecommendedGameLinks();
        gameRepository.deleteAllGameGenres();
        gameRepository.deleteAllGames();
    }
}
