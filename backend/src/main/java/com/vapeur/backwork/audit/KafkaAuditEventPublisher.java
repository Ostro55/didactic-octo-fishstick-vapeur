package com.vapeur.backwork.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "audit.kafka.enabled", havingValue = "true")
public class KafkaAuditEventPublisher implements AuditEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${audit.topic:audit-events}")
    private String topic;

    @Override
    public void publish(AuditEvent event) {
        // Keying by resourceId helps keep per-resource ordering in the topic.
        String key = event.resourceId() != null ? event.resourceId() : event.eventId();
        if (TransactionSynchronizationManager.isSynchronizationActive() && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send(topic, key, event);
                }
            });
            return;
        }
        kafkaTemplate.send(topic, key, event);
    }
}
