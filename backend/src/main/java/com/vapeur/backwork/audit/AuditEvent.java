package com.vapeur.backwork.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
        String eventId,
        Instant occurredAt,
        AuditActorType actorType,
        String actorId,
        String actorUsername,
        Boolean actorAdmin,
        AuditAction action,
        AuditResourceType resourceType,
        String resourceId,
        Map<String, Object> metadata
) {
}

