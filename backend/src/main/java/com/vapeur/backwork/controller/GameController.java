package com.vapeur.backwork.controller;

import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("game/all")
    public ResponseEntity<List<Game>> getAll(
            @RequestParam String name,
            @RequestParam String genre,
            @RequestParam Long minPrice,
            @RequestParam Long maxPrice
    ) {
        List<Game> allGames;
        if (name == null && genre == null && minPrice == null && maxPrice == null)
            allGames = gameService.getAll();
        else
            allGames = gameService.getAllWithFilters(name, minPrice, maxPrice, genre);
        return new ResponseEntity<>(allGames, HttpStatus.OK);
    }

    @PostMapping("game/save")
    public ResponseEntity<Game> save(@RequestBody Game newGame) {
        Optional<Game> game = gameService.addGame(newGame);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED)).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @GetMapping("game/{id}")
    public ResponseEntity<Game> getbyId(@PathVariable("id") Long id) {
        Optional<Game> game = gameService.getById(id);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
