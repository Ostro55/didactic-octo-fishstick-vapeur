package com.vapeur.backwork.repository;

import com.vapeur.backwork.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
