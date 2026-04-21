package com.vapeur.backwork.pipeline;

import com.vapeur.backwork.audit.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("NoopAuditEventPublisher")
class NoopAuditEventPublisherTest {

	private final NoopAuditEventPublisher publisher = new NoopAuditEventPublisher();

	@Test
	@DisplayName("publish() ne lance aucune exception")
	void publish_doesNotThrow() {
		AuditEvent event = new AuditEvent(
				"evt-1",
				Instant.now(),
				AuditActorType.SYSTEM,
				null,
				"system",
				null,
				AuditAction.GAME_CREATED,
				AuditResourceType.GAME,
				"42",
				Map.of("name", "Doom")
		);

		assertDoesNotThrow(() -> publisher.publish(event));
	}

	@Test
	@DisplayName("publish() avec un événement null-resource ne lance pas d'exception")
	void publish_withNullResource_doesNotThrow() {
		AuditEvent event = new AuditEvent(
				"evt-2",
				Instant.now(),
				AuditActorType.SYSTEM,
				null,
				"system",
				null,
				AuditAction.GAMES_CLEANED,
				AuditResourceType.SYSTEM,
				null,
				Map.of()
		);

		assertDoesNotThrow(() -> publisher.publish(event));
	}

	@Test
	@DisplayName("publish() avec un événement USER → ne lance pas d'exception")
	void publish_userEvent_doesNotThrow() {
		AuditEvent event = AuditEvents.user(
				"10",
				"alice",
				false,
				AuditAction.GAME_SUBMITTED,
				AuditResourceType.GAME,
				"5",
				Map.of("name", "Celeste", "status", "pending")
		);

		assertDoesNotThrow(() -> publisher.publish(event));
	}
}