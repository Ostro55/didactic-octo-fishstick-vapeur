package com.vapeur.backwork.audit;

public interface AuditEventPublisher {
    void publish(AuditEvent event);
}

