package com.vapeur.backwork.controller;

import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("game/all")
    public ResponseEntity<List<Game>> getAll() {
        List<Game> allGames = gameService.getAll();
        return new ResponseEntity<>(allGames, HttpStatus.OK);
    }

    @PostMapping("game/save")
    public ResponseEntity<Game> save(@RequestBody Game newGame) {
        Optional<Game> game = gameService.addGame(newGame);
        return game.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED)).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
