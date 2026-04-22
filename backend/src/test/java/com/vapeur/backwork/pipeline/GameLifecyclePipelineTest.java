package com.vapeur.backwork.pipeline;

import com.vapeur.backwork.utils.GameGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for full game lifecycle :
 * user submit -> status = pending
 * admin accept -> status = accepted
 * OR
 * admin reject -> delete bdd
 */

@SpringBootTest(properties = {
		"spring.profiles.active=h2",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@DisplayName("Game lifecycle pipeline")
class GameLifecyclePipelineTest {

	@Autowired
	MockMvc mvc;

	// -------------------------------------------------------------------------
	// Setup
	// -------------------------------------------------------------------------

	@BeforeEach
	void cleanDb() throws Exception {
		mvc.perform(delete("/games")).andExpect(status().isNoContent());
		mvc.perform(delete("/users")).andExpect(status().isNoContent());
	}

	// =========================================================================
	// Etape 1 - User submit his game
	// =========================================================================

	@Nested
	@DisplayName("Etape 1 - User submit his game")
	class Submission {

		@Test
		@DisplayName("A non-admin user submits a game -> status is 'pending'")
		void userSubmit_setsPending() throws Exception {
			long userId = createUser("alice", "alice@mail.com", false);

			mvc.perform(post("/games/save").param("userId", String.valueOf(userId))
							.contentType(MediaType.APPLICATION_JSON)
							.content(gameJson("Celeste", 20, GameGenre.action)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.status").value("pending"))
					.andExpect(jsonPath("$.name").value("Celeste"));
		}

		@Test
		@DisplayName("An admin submits a game -> status is 'accepted' immediately")
		void adminSubmit_setsAccepted() throws Exception {
			long adminId = createUser("admin", "admin@mail.com", true);

			mvc.perform(post("/games/save").param("userId", String.valueOf(adminId))
							.contentType(MediaType.APPLICATION_JSON)
							.content(gameJson("Doom", 40, GameGenre.action, GameGenre.horror)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.status").value("accepted"))
					.andExpect(jsonPath("$.genre", containsInAnyOrder("action", "horror")));
		}

		@Test
		@DisplayName("Client-provided status is ignored — non-admin always gets 'pending'")
		void userSubmit_ignoredClientStatus_remainsPending() throws Exception {
			long userId = createUser("bob", "bob@mail.com", false);

			// Même si le payload envoie "accepted", le service doit l'ignorer
			String payload = "{\"name\":\"Hades\",\"price\":25,\"status\":\"accepted\",\"genre\":[\"action\"]}";

			mvc.perform(post("/games/save").param("userId", String.valueOf(userId))
							.contentType(MediaType.APPLICATION_JSON)
							.content(payload))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.status").value("pending"));
		}

		@Test
		@DisplayName("Submitting with an unknown userId -> 404")
		void submit_unknownUser_returns404() throws Exception {
			mvc.perform(post("/games/save").param("userId", "999999")
							.contentType(MediaType.APPLICATION_JSON)
							.content(gameJson("Ghost of Tsushima", 50, GameGenre.action)))
					.andExpect(status().isNotFound());
		}
	}

	// =========================================================================
	// Etape 2 - Admin accepts a pending game
	// =========================================================================

	@Nested
	@DisplayName("Etape 2 - Admin accepts a pending game")
	class Validation {

		@Test
		@DisplayName("Admin accepts a pending game -> status switches to 'accepted'")
		void adminAccepts_pendingGame_becomesAccepted() throws Exception {
			long userId = createUser("carol", "carol@mail.com", false);
			long gameId = submitGame(userId, "Hollow Knight", 15, GameGenre.action);

			mvc.perform(put("/games/" + gameId + "/accept"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("accepted"))
					.andExpect(jsonPath("$.id").value(gameId));
		}

		@Test
		@DisplayName("Accepted game is persisted — subsequent GET reflects the change")
		void accepted_gameIsPersisted() throws Exception {
			long userId = createUser("dan", "dan@mail.com", false);
			long gameId = submitGame(userId, "Stardew Valley", 12, GameGenre.singleplayer);

			mvc.perform(put("/games/" + gameId + "/accept"))
					.andExpect(status().isOk());

			mvc.perform(get("/games/" + gameId))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("accepted"));
		}

		@Test
		@DisplayName("Accepting a non-existent game -> 500 (contrat API actuel)")
		void accept_nonExistentGame_returns500() throws Exception {
			mvc.perform(put("/games/999999/accept"))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("Full pipeline : user submits -> admin accepts -> game visible as accepted")
		void fullPipeline_submitThenAccept() throws Exception {
			long userId = createUser("frank", "frank@mail.com", false);
			long gameId = submitGame(userId, "Elden Ring", 60, GameGenre.action);

			// 1. Après soumission -> pending
			mvc.perform(get("/games/" + gameId))
					.andExpect(jsonPath("$.status").value("pending"));

			// 2. Admin accepte
			mvc.perform(put("/games/" + gameId + "/accept"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("accepted"));

			// 3. Confirmé via GET
			mvc.perform(get("/games/" + gameId))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("accepted"))
					.andExpect(jsonPath("$.name").value("Elden Ring"));
		}
	}

	// =========================================================================
	// Méthodes utilitaires
	// =========================================================================

	/** Crée un utilisateur et retourne son id. */
	private long createUser(String username, String email, boolean isAdmin) throws Exception {
		MvcResult result = mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson(username, email, isAdmin)))
				.andExpect(status().isCreated())
				.andReturn();

		Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return id.longValue();
	}

	/** Soumet un jeu via POST /games/save et retourne l'id du jeu créé. */
	private long submitGame(long userId, String name, long price, GameGenre... genres) throws Exception {
		MvcResult result = mvc.perform(post("/games/save").param("userId", String.valueOf(userId))
						.contentType(MediaType.APPLICATION_JSON)
						.content(gameJson(name, price, genres)))
				.andExpect(status().isCreated())
				.andReturn();

		Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return id.longValue();
	}

	private static String userJson(String username, String email, boolean isAdmin) {
		return "{"
				+ "\"username\":" + q(username)
				+ ",\"password\":" + q("pass")
				+ ",\"email\":" + q(email)
				+ ",\"isAdmin\":" + isAdmin
				+ ",\"recommendedGames\":[]"
				+ "}";
	}

	private static String gameJson(String name, long price, GameGenre... genres) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"name\":").append(q(name));
		sb.append(",\"price\":").append(price);
		sb.append(",\"genre\":[");
		for (int i = 0; i < genres.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(q(genres[i].name()));
		}
		sb.append("]}");
		return sb.toString();
	}

	private static String q(String s) {
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
}