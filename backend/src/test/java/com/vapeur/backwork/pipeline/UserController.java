package com.vapeur.backwork.pipeline;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
		"spring.profiles.active=h2",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@DisplayName("UserController — cas manquants")
class UserController {

	@Autowired
	MockMvc mvc;

	@BeforeEach
	void cleanDb() throws Exception {
		mvc.perform(delete("/users")).andExpect(status().isNoContent());
		mvc.perform(delete("/games")).andExpect(status().isNoContent());
	}

	// =========================================================================
	// Login
	// =========================================================================

	@Nested
	@DisplayName("POST /users/login")
	class Login {

		@Test
		@DisplayName("Mauvais mot de passe → 404")
		void login_wrongPassword_returns404() throws Exception {
			mvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("alice", "alice@mail.com", false, "correct_password")))
					.andExpect(status().isCreated());

			mvc.perform(post("/users/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(loginJson("alice@mail.com", "wrong_password")))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Email inconnu → 404")
		void login_unknownEmail_returns404() throws Exception {
			mvc.perform(post("/users/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(loginJson("nobody@mail.com", "anypassword")))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Bon email + bon mot de passe → 200, password non exposé")
		void login_correctCredentials_returns200_noPassword() throws Exception {
			mvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("bob", "bob@mail.com", false, "secret")))
					.andExpect(status().isCreated());

			mvc.perform(post("/users/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(loginJson("bob@mail.com", "secret")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.username").value("bob"))
					.andExpect(jsonPath("$.password").doesNotExist());
		}
	}

	// =========================================================================
	// Username en double
	// =========================================================================

	@Nested
	@DisplayName("POST /users — contrainte unique")
	class UniqueConstraint {

		@Test
		@DisplayName("Username en double → erreur (500)")
		void save_duplicateUsername_returnsError() throws Exception {
			mvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("carol", "carol@mail.com", false, "pw")))
					.andExpect(status().isCreated());

			mvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("carol", "carol2@mail.com", false, "pw")))
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("Email en double → erreur (500)")
		void save_duplicateEmail_returnsError() throws Exception {
			mvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("dan", "shared@mail.com", false, "pw")))
					.andExpect(status().isCreated());

			mvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("dan2", "shared@mail.com", false, "pw")))
					.andExpect(status().is5xxServerError());
		}
	}

	// =========================================================================
	// PUT & DELETE sur ID inconnu
	// =========================================================================

	@Nested
	@DisplayName("PUT & DELETE /users/{id} — ID inconnu")
	class UnknownId {

		@Test
		@DisplayName("PUT /users/{id} inconnu → 500")
		void update_unknownUser_returns500() throws Exception {
			mvc.perform(put("/users/999999")
							.contentType(MediaType.APPLICATION_JSON)
							.content(userJson("ghost", "ghost@mail.com", false, "pw")))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("DELETE /users/{id} inconnu → 500")
		void delete_unknownUser_returns500() throws Exception {
			mvc.perform(delete("/users/999999"))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("GET /users/{id} inconnu → 500")
		void getById_unknownUser_returns500() throws Exception {
			mvc.perform(get("/users/999999"))
					.andExpect(status().isInternalServerError());
		}
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	private long createUser(String username, String email) throws Exception {
		MvcResult result = mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson(username, email, false, "pw")))
				.andExpect(status().isCreated())
				.andReturn();

		Number id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		return id.longValue();
	}

	private static String userJson(String username, String email, boolean isAdmin, String password) {
		return "{"
				+ "\"username\":" + q(username)
				+ ",\"password\":" + q(password)
				+ ",\"email\":" + q(email)
				+ ",\"isAdmin\":" + isAdmin
				+ ",\"recommendedGames\":[]"
				+ "}";
	}

	private static String loginJson(String email, String password) {
		return "{\"email\":" + q(email) + ",\"password\":" + q(password) + "}";
	}

	private static String q(String s) {
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
}