package com.vapeur.backwork.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "audit.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoopAuditEventPublisher implements AuditEventPublisher {
    @Override
    public void publish(AuditEvent event) {
        // Intentionally no-op (used when audit.kafka.enabled is false).
    }
}
