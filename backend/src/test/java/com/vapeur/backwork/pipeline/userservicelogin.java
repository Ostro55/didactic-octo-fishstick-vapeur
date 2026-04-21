package com.vapeur.backwork.pipeline;

import com.vapeur.backwork.RequestDto.UserRequestDto;
import com.vapeur.backwork.audit.AuditEventPublisher;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.UserRepository;
import com.vapeur.backwork.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — login")
class Userservicelogin {

	@Mock UserRepository userRepository;
	@Mock AuditEventPublisher auditEventPublisher;

	@InjectMocks
	UserService userService;

	@Nested
	@DisplayName("login()")
	class Login {

		@Test
		@DisplayName("Email + mot de passe corrects → retourne l'utilisateur")
		void login_correctCredentials_returnsUser() {
			User alice = user("alice", "alice@mail.com", "secret");
			when(userRepository.findAll()).thenReturn(List.of(alice));

			Optional<User> result = userService.login(new UserRequestDto("alice@mail.com", "secret"));

			assertTrue(result.isPresent());
			assertEquals("alice", result.get().getUsername());
		}

		@Test
		@DisplayName("Mauvais mot de passe → retourne vide")
		void login_wrongPassword_returnsEmpty() {
			User alice = user("alice", "alice@mail.com", "correct");
			when(userRepository.findAll()).thenReturn(List.of(alice));

			Optional<User> result = userService.login(new UserRequestDto("alice@mail.com", "wrong"));

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("Email inconnu → retourne vide")
		void login_unknownEmail_returnsEmpty() {
			when(userRepository.findAll()).thenReturn(List.of());

			Optional<User> result = userService.login(new UserRequestDto("nobody@mail.com", "any"));

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("Bon email, mauvais mot de passe parmi plusieurs users → retourne vide")
		void login_correctEmailWrongPassword_multipleUsers_returnsEmpty() {
			User alice = user("alice", "alice@mail.com", "alicepw");
			User bob   = user("bob",   "bob@mail.com",   "bobpw");
			when(userRepository.findAll()).thenReturn(List.of(alice, bob));

			Optional<User> result = userService.login(new UserRequestDto("alice@mail.com", "bobpw"));

			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("Plusieurs users — retourne uniquement celui qui correspond")
		void login_multipleUsers_returnsMatchingOne() {
			User alice = user("alice", "alice@mail.com", "alicepw");
			User bob   = user("bob",   "bob@mail.com",   "bobpw");
			when(userRepository.findAll()).thenReturn(List.of(alice, bob));

			Optional<User> result = userService.login(new UserRequestDto("bob@mail.com", "bobpw"));

			assertTrue(result.isPresent());
			assertEquals("bob", result.get().getUsername());
		}

		@Test
		@DisplayName("login ne publie aucun événement audit")
		void login_doesNotPublishAuditEvent() {
			when(userRepository.findAll()).thenReturn(List.of());

			userService.login(new UserRequestDto("x@mail.com", "pw"));

			verify(auditEventPublisher, never()).publish(any());
		}
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	private static User user(String username, String email, String password) {
		User u = new User();
		u.setUsername(username);
		u.setEmail(email);
		u.setPassword(password);
		u.setAdmin(false);
		return u;
	}
}