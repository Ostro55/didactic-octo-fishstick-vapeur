package com.vapeur.backwork.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class AuditEvents {
    private AuditEvents() {}

    public static AuditEvent system(AuditAction action, AuditResourceType resourceType, String resourceId, Map<String, Object> metadata) {
        return new AuditEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                AuditActorType.SYSTEM,
                null,
                "system",
                null,
                action,
                resourceType,
                resourceId,
                metadata
        );
    }

    public static AuditEvent user(
            String actorId,
            String actorUsername,
            boolean actorAdmin,
            AuditAction action,
            AuditResourceType resourceType,
            String resourceId,
            Map<String, Object> metadata
    ) {
        return new AuditEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                AuditActorType.USER,
                actorId,
                actorUsername,
                actorAdmin,
                action,
                resourceType,
                resourceId,
                metadata
        );
    }
}

