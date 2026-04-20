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

    @GetMapping("games")
    public ResponseEntity<List<Game>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice
    ) {
        List<Game> allGames;
        if (name == null && genre == null && minPrice == null && maxPrice == null)
            allGames = gameService.getAll();
        else
            allGames = gameService.getAllWithFilters(name, minPrice, maxPrice, genre);
        return new ResponseEntity<>(allGames, HttpStatus.OK);
    }

    @PostMapping("games")
    public ResponseEntity<Game> save(@RequestBody Game newGame) {
        Optional<Game> game = gameService.addGame(newGame);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED)).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PostMapping("games/save")
    public ResponseEntity<Game> saveWithUser(@RequestParam("userId") Long userId, @RequestBody Game newGame) {
        // The effective status is derived in the service from the calling user role.
        Optional<Game> game = gameService.addGame(newGame, userId);
        if (game.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(game.get(), HttpStatus.CREATED);
    }

    @GetMapping("games/{id}")
    public ResponseEntity<Game> getbyId(@PathVariable("id") Long id) {
        Optional<Game> game = gameService.getById(id);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @DeleteMapping("games")
    public ResponseEntity<Void> clean() {
        gameService.cleanGames();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("games/{id}/accept")
    public ResponseEntity<Game> acceptGame(@PathVariable("id") Long id) {
        // Keep the current API contract: missing game currently maps to 500 here.
        Optional<Game> game = gameService.acceptGame(id);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
