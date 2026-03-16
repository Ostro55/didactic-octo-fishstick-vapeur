package com.vapeur.backwork.service;

import com.vapeur.backwork.entity.Game;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameFiltersTest {

    @Test
    void apply_nameFilter_isEqualsIgnoreCase() {
        Game a = new Game();
        a.setName("Doom");

        Game b = new Game();
        b.setName("doOm");

        Game c = new Game();
        c.setName("Doom Eternal");

        List<Game> out = GameFilters.apply(List.of(a, b, c), "doom", null, null, null);
        assertEquals(List.of(a, b), out);
    }

    @Test
    void apply_priceFilters_excludeNullPrices() {
        Game a = new Game();
        a.setName("A");
        a.setPrice(10L);

        Game b = new Game();
        b.setName("B");
        b.setPrice(null);

        Game c = new Game();
        c.setName("C");
        c.setPrice(30L);

        List<Game> out = GameFilters.apply(List.of(a, b, c), null, 15L, 30L, null);
        assertEquals(List.of(c), out);
    }

    @Test
    void apply_genreFilter_isCaseInsensitive() {
        Game a = new Game();
        a.setName("A");
        a.setGenre(Set.of("action", "horror"));

        Game b = new Game();
        b.setName("B");
        b.setGenre(Set.of("romance"));

        List<Game> out = GameFilters.apply(List.of(a, b), null, null, null, "AcTiOn");
        assertEquals(List.of(a), out);
    }

    @Test
    void apply_blankFilters_areIgnored() {
        Game a = new Game();
        a.setName("A");
        a.setPrice(10L);
        a.setGenre(Set.of("action"));

        List<Game> out = GameFilters.apply(List.of(a), "   ", null, null, " ");
        assertEquals(List.of(a), out);
    }
}

