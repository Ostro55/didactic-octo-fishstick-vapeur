package com.vapeur.backwork.dto;

import tools.jackson.databind.JsonNode;

/**
 * One record read from the audit Kafka topic.
 *
 * If {@code event} is null, {@code raw} contains the unparsed Kafka value.
 */
public record AuditKafkaRecordDto(
        int partition,
        long offset,
        long timestamp,
        String key,
        JsonNode event,
        String raw
) {
}
