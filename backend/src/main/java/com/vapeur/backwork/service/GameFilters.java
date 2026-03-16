package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;

import java.util.List;
import java.util.Objects;

final class GameFilters {

    private GameFilters() {
    }

    static List<Game> apply(List<Game> games, String name, Long minPrice, Long maxPrice, String genre) {
        String nameFilter = normalize(name);
        String genreFilter = normalize(genre);

        var stream = games.stream();

        if (nameFilter != null) {
            stream = stream.filter(g -> g.getName() != null && g.getName().equalsIgnoreCase(nameFilter));
        }
        if (minPrice != null) {
            stream = stream.filter(g -> g.getPrice() != null && g.getPrice() >= minPrice);
        }
        if (maxPrice != null) {
            stream = stream.filter(g -> g.getPrice() != null && g.getPrice() <= maxPrice);
        }
        if (genreFilter != null) {
            stream = stream.filter(g ->
                    g.getGenre() != null
                            && g.getGenre().stream()
                            .filter(Objects::nonNull)
                            .anyMatch(x -> x.equalsIgnoreCase(genreFilter))
            );
        }

        return stream.toList();
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

