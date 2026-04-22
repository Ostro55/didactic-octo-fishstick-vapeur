package com.vapeur.backwork.pipeline;

import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.entity.Game;
import com.vapeur.backwork.repository.GameRepository;
import com.vapeur.backwork.repository.UserRepository;
import com.vapeur.backwork.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameService — cas manquants")
class gameservice {

	@Mock GameRepository gameRepository;
	@Mock UserRepository userRepository;
	@Mock AuditEventPublisher auditEventPublisher;

	@InjectMocks
	GameService gameService;

	// =========================================================================
	// acceptGame
	// =========================================================================

	@Nested
	@DisplayName("acceptGame")
	class AcceptGame {

		@Test
		@DisplayName("Jeu trouvé → status passe à 'accepted' et est sauvegardé")
		void acceptGame_found_setsAcceptedAndSaves() {
			Game game = game("Zelda", "pending");
			game.setId(1L);
			when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
			when(gameRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			Optional<Game> result = gameService.acceptGame(1L);

			assertTrue(result.isPresent());
			assertEquals("accepted", result.get().getStatus());
			verify(gameRepository).save(game);
			verify(auditEventPublisher).publish(any());
		}

		@Test
		@DisplayName("Jeu non trouvé → retourne vide, publie un audit REJECTED")
		void acceptGame_notFound_returnsEmpty_andPublishesRejected() {
			when(gameRepository.findById(999L)).thenReturn(Optional.empty());

			Optional<Game> result = gameService.acceptGame(999L);

			assertTrue(result.isEmpty());
			verify(gameRepository, never()).save(any());
			// Un audit GAMES_REJECTED est quand même publié
			verify(auditEventPublisher).publish(any());
		}
	}

	// =========================================================================
	// deleteGameById
	// =========================================================================

	@Nested
	@DisplayName("deleteGameById")
	class DeleteGameById {

		@Test
		@DisplayName("Jeu trouvé → supprimé et retourné")
		void deleteGameById_found_deletesAndReturns() {
			Game game = game("Hades", "accepted");
			game.setId(2L);
			when(gameRepository.findById(2L)).thenReturn(Optional.of(game));

			Optional<Game> result = gameService.deleteGameById(2L);

			assertTrue(result.isPresent());
			assertSame(game, result.get());
			verify(gameRepository).delete(game);
			verify(auditEventPublisher).publish(any());
		}

		@Test
		@DisplayName("Jeu non trouvé → retourne vide, rien n'est supprimé")
		void deleteGameById_notFound_returnsEmpty_noDelete() {
			when(gameRepository.findById(999L)).thenReturn(Optional.empty());

			Optional<Game> result = gameService.deleteGameById(999L);

			assertTrue(result.isEmpty());
			verify(gameRepository, never()).delete(any(Game.class));
			verify(auditEventPublisher, never()).publish(any());
		}
	}

	// =========================================================================
	// cleanGames
	// =========================================================================

	@Nested
	@DisplayName("cleanGames")
	class CleanGames {

		@Test
		@DisplayName("Supprime les liens recommandés, les genres, puis les jeux — dans cet ordre")
		void cleanGames_deletesInCorrectOrder() {
			gameService.cleanGames();

			var inOrder = inOrder(gameRepository);
			inOrder.verify(gameRepository).deleteAllRecommendedGameLinks();
			inOrder.verify(gameRepository).deleteAllGameGenres();
			inOrder.verify(gameRepository).deleteAllGames();
			verify(auditEventPublisher).publish(any());
		}
	}

	// =========================================================================
	// addGame (sans userId)
	// =========================================================================

	@Nested
	@DisplayName("addGame sans userId")
	class AddGameNoUser {

		@Test
		@DisplayName("Sauvegarde le jeu et publie un audit GAME_CREATED")
		void addGame_savesAndPublishesAudit() {
			Game game = game("Celeste", "pending");
			when(gameRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			Optional<Game> result = gameService.addGame(game);

			assertTrue(result.isPresent());
			verify(gameRepository).save(game);
			verify(auditEventPublisher).publish(any());
		}
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	private static Game game(String name, String status) {
		Game g = new Game();
		g.setName(name);
		g.setPrice(10L);
		g.setStatus(status);
		return g;
	}
}