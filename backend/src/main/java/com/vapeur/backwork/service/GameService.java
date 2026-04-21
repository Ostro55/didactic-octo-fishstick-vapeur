package com.vapeur.backwork.service;

import com.vapeur.backwork.audit.*;
import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService implements IGameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final AuditEventPublisher auditEventPublisher;

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
        auditEventPublisher.publish(AuditEvents.system(
                AuditAction.GAME_CREATED,
                AuditResourceType.GAME,
                newGame.getId() == null ? null : newGame.getId().toString(),
                Map.of("name", newGame.getName(), "status", newGame.getStatus())
        ));
        return Optional.of(newGame);
    }

    @Override
    public Optional<Game> addGame(Game newGame, Long userId) {
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return Optional.empty();

            User actor = userOpt.get();
            // Never trust client-provided status: derive it from the calling user.
            newGame.setStatus(actor.isAdmin() ? "accepted" : "pending");
            auditEventPublisher.publish(AuditEvents.user(
                actor.getId() == null ? null : actor.getId().toString(),
                actor.getUsername(),
                actor.isAdmin(),
                AuditAction.GAME_SUBMITTED,
                AuditResourceType.GAME,
                newGame.getId() == null ? null : newGame.getId().toString(),
                Map.of(
                        "name", newGame.getName(),
                        "status", newGame.getStatus()
                )
            ));
        } else {
            auditEventPublisher.publish(AuditEvents.user(
                    null,
                    "UNKNOWN",
                    false,
                    AuditAction.GAME_SUBMITTED,
                    AuditResourceType.GAME,
                    newGame.getId() == null ? null : newGame.getId().toString(),
                    Map.of(
                            "name", newGame.getName(),
                            "status", newGame.getStatus()
                    )
            ));
        }

        gameRepository.save(newGame);
        return Optional.of(newGame);
    }

    @Override
    @Transactional
    public Optional<Game> deleteGameById(Long id) {
        Optional<Game> game = gameRepository.findById(id);
        game.ifPresent(g -> {
            gameRepository.deleteRecommendedGameLinksForGame(g.getId());
            gameRepository.deleteGenresForGame(g.getId());
            gameRepository.delete(g);
            auditEventPublisher.publish(AuditEvents.system(
                    AuditAction.GAME_DELETED,
                    AuditResourceType.GAME,
                    g.getId() == null ? null : g.getId().toString(),
                    Map.of("name", g.getName(), "status", g.getStatus())
            ));
        });
        return game;
    }

    @Override
    @Transactional
    public void cleanGames() {
        // Order matters because of foreign keys.
        gameRepository.deleteAllRecommendedGameLinks();
        gameRepository.deleteAllGameGenres();
        gameRepository.deleteAllGames();
        auditEventPublisher.publish(AuditEvents.system(
                AuditAction.GAMES_CLEANED,
                AuditResourceType.SYSTEM,
                null,
                Map.of()
        ));
    }

    @Override
    public Optional<Game> acceptGame(Long id) {
        Optional<Game> optGame = gameRepository.findById(id);
        if (optGame.isEmpty()) {
            auditEventPublisher.publish(AuditEvents.system(
                    AuditAction.GAMES_REJECTED,
                    AuditResourceType.GAME,
                    null,
                    Map.of()
            ));

            return  Optional.empty();
        } else {
            Game game = optGame.get();
            game.setStatus("accepted");
            game = gameRepository.save(game);

            auditEventPublisher.publish(AuditEvents.system(
                    AuditAction.GAMES_ACCEPTED,
                    AuditResourceType.GAME,
                    game.getId() == null ? null : game.getId().toString(),
                    Map.of("name", game.getName(), "status", game.getStatus())
            ));

            return Optional.of(game);
        }
    }

    @Override
    @Transactional
    public Optional<Game> rejectGame(Long id) {
        Optional<Game> optGame = gameRepository.findById(id);
        if (optGame.isEmpty()) {
            return Optional.empty();
        }

        Game game = optGame.get();
        gameRepository.deleteRecommendedGameLinksForGame(game.getId());
        gameRepository.deleteGenresForGame(game.getId());
        gameRepository.delete(game);

        auditEventPublisher.publish(AuditEvents.system(
                AuditAction.GAMES_REJECTED,
                AuditResourceType.GAME,
                game.getId() == null ? null : game.getId().toString(),
                Map.of("name", game.getName(), "status", game.getStatus())
        ));

        return Optional.of(game);
    }
}
