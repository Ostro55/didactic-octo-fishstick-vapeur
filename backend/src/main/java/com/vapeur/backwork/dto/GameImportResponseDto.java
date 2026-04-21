package com.vapeur.backwork.dto;

import com.vapeur.backwork.entity.Game;

import java.util.List;

public record GameImportResponseDto(
        int importedCount,
        List<Game> games
) {
}
