package com.vapeur.backwork.service;

import com.vapeur.backwork.audit.*;
import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.repository.UserRepository;
import com.vapeur.backwork.utils.GameGenre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

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
    public List<Game> importGamesFromCsv(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier CSV est vide.");
        }

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable."));
        if (!admin.isAdmin()) {
            throw new IllegalStateException("L'utilisateur doit etre admin pour importer des jeux.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Le fichier CSV est vide.");
            }

            List<String> headers = parseCsvLine(headerLine);
            validateHeaders(headers);

            List<Game> importedGames = reader.lines()
                    .filter(line -> !line.isBlank())
                    .map(line -> toGame(line, headers))
                    .map(gameRepository::save)
                    .toList();

            importedGames.forEach(game -> auditEventPublisher.publish(AuditEvents.user(
                    admin.getId() == null ? null : admin.getId().toString(),
                    admin.getUsername(),
                    true,
                    AuditAction.GAME_CREATED,
                    AuditResourceType.GAME,
                    game.getId() == null ? null : game.getId().toString(),
                    Map.of("name", game.getName(), "status", game.getStatus())
            )));

            return importedGames;
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de lire le fichier CSV.", e);
        }
    }

    private static void validateHeaders(List<String> headers) {
        List<String> expected = List.of("name", "price", "description", "release_date", "img_url", "editor", "genre", "status");
        if (!headers.equals(expected)) {
            throw new IllegalArgumentException("En-tetes CSV invalides. Attendu: " + String.join(",", expected));
        }
    }

    private Game toGame(String line, List<String> headers) {
        List<String> values = parseCsvLine(line);
        if (values.size() != headers.size()) {
            throw new IllegalArgumentException("Ligne CSV invalide: nombre de colonnes incorrect.");
        }

        Game game = new Game();
        game.setName(requiredValue(values.get(0), "name"));
        game.setPrice(parsePrice(values.get(1)));
        game.setDescription(blankToNull(values.get(2)));
        game.setRelease_date(parseTimestamp(values.get(3)));
        game.setImg_url(blankToNull(values.get(4)));
        game.setEditor(blankToNull(values.get(5)));
        game.setGenre(parseGenres(values.get(6)));
        String status = blankToNull(values.get(7));
        game.setStatus(status == null ? "accepted" : status);
        return game;
    }

    private static String requiredValue(String value, String fieldName) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException("Champ obligatoire manquant: " + fieldName);
        }
        return trimmed;
    }

    private static Long parsePrice(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Prix invalide: " + value, e);
        }
    }

    private static Timestamp parseTimestamp(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            if (trimmed.length() == 10) {
                return Timestamp.valueOf(LocalDate.parse(trimmed).atStartOfDay());
            }
            return Timestamp.valueOf(LocalDateTime.parse(trimmed));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("release_date invalide: " + value + ". Utiliser yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ss", e);
        }
    }

    private static Set<GameGenre> parseGenres(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return Set.of();
        }

        try {
            return java.util.Arrays.stream(trimmed.split("\\|"))
                    .map(String::trim)
                    .filter(part -> !part.isEmpty())
                    .map(GameGenre::valueOf)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Genre invalide: " + value, e);
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<String> parseCsvLine(String line) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Ligne CSV invalide: guillemets non fermes.");
        }

        values.add(current.toString());
        return values;
    }
}
