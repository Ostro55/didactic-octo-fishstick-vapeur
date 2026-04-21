package com.vapeur.backwork.repository;

import com.vapeur.backwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Modifying
    @Query(value = "delete from user_recommended_games where user_id = :userId", nativeQuery = true)
    int deleteRecommendedGameLinksForUser(@Param("userId") Long userId);

    @Modifying
    @Query(value = "delete from user_recommended_games", nativeQuery = true)
    int deleteAllRecommendedGameLinks();

    @Modifying
    @Query(value = "delete from users", nativeQuery = true)
    int deleteAllUsers();
}
