package com.vapeur.backwork.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class KafkaAuditEventPublisherTest {

    @AfterEach
    void cleanupTxSync() {
        // Keep tests isolated even if an assertion fails mid-way.
        try {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void publish_withoutTransaction_sendsImmediately_keyIsResourceId() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaAuditEventPublisher publisher = new KafkaAuditEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "topic", "audit-events");

        AuditEvent event = new AuditEvent(
                "evt-1",
                Instant.now(),
                AuditActorType.SYSTEM,
                null,
                "system",
                null,
                AuditAction.USER_CREATED,
                AuditResourceType.USER,
                "123",
                Map.of("username", "alice")
        );

        publisher.publish(event);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.Mockito.eq("audit-events"), org.mockito.Mockito.eq("123"), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("\"action\":\"USER_CREATED\""));
    }

    @Test
    void publish_withoutTransaction_keyFallsBackToEventId_whenResourceIdNull() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaAuditEventPublisher publisher = new KafkaAuditEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "topic", "audit-events");

        AuditEvent event = new AuditEvent(
                "evt-xyz",
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

        publisher.publish(event);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.Mockito.eq("audit-events"), org.mockito.Mockito.eq("evt-xyz"), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("\"action\":\"GAMES_CLEANED\""));
    }

    @Test
    void publish_withTransaction_defersSendUntilAfterCommit() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaAuditEventPublisher publisher = new KafkaAuditEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(publisher, "topic", "audit-events");

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        AuditEvent event = AuditEvents.system(
                AuditAction.USER_UPDATED,
                AuditResourceType.USER,
                "42",
                Map.of("username", "bob")
        );

        publisher.publish(event);
        verify(kafkaTemplate, never()).send(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString(), org.mockito.Mockito.any());

        List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
        assertTrue(syncs.size() >= 1);
        syncs.forEach(TransactionSynchronization::afterCommit);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());
        assertEquals("audit-events", topicCaptor.getValue());
        assertEquals("42", keyCaptor.getValue());
        assertTrue(valueCaptor.getValue().contains("\"action\":\"USER_UPDATED\""));
    }
}
