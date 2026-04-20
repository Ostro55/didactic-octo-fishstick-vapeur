package com.vapeur.backwork.RequestDto;

import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.entity.User;

import java.util.Set;

public record UserResponseDto(Long id, String username, boolean isAdmin, String email, Set<Game> recommendedGames) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.isAdmin(),
                user.getEmail(),
                user.getRecommendedGames()
        );
    }
}
