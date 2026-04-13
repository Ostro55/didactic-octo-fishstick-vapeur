package com.vapeur.backwork.service;

import com.vapeur.backwork.audit.AuditAction;
import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.audit.AuditEvents;
import com.vapeur.backwork.audit.AuditResourceType;
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
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Optional.empty();

        User actor = userOpt.get();
        // Never trust client-provided status: derive it from the calling user.
        newGame.setStatus(actor.isAdmin() ? "accepted" : "pending");
        gameRepository.save(newGame);
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
        return Optional.of(newGame);
    }

    @Override
    public Optional<Game> deleteGameById(Long id) {
        Optional<Game> game = gameRepository.findById(id);
        game.ifPresent(g -> {
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
}
