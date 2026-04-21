package com.vapeur.backwork.repository;

import com.vapeur.backwork.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Modifying
    @Query(value = "delete from user_recommended_games where game_id = ?1", nativeQuery = true)
    int deleteRecommendedGameLinksForGame(Long gameId);

    @Modifying
    @Query(value = "delete from game_genres where game_id = ?1", nativeQuery = true)
    int deleteGenresForGame(Long gameId);

    @Modifying
    @Query(value = "delete from user_recommended_games", nativeQuery = true)
    int deleteAllRecommendedGameLinks();

    @Modifying
    @Query(value = "delete from game_genres", nativeQuery = true)
    int deleteAllGameGenres();

    @Modifying
    @Query(value = "delete from games", nativeQuery = true)
    int deleteAllGames();
}
