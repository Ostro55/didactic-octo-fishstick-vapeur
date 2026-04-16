package com.vapeur.backwork.entity;

import com.vapeur.backwork.utils.GameGenre;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.sql.Timestamp;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String name;
    private Long price;
    private String description;
    private Timestamp release_date;
    private String img_url;
    private String editor;
    private String status = "pending";

    @ElementCollection(fetch = FetchType.EAGER, targetClass = GameGenre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "genre", nullable = false)
    private Set<GameGenre> genre;
}
