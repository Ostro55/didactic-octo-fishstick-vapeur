package com.vapeur.backwork.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private boolean isAdmin;

    @NonNull
    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_recommended_games",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private Set<Game> recommendedGames = new HashSet<>();

    @com.fasterxml.jackson.annotation.JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isAdmin")
    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }
}
