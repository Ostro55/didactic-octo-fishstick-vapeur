package com.vapeur.backwork.dto;

import java.util.List;

public record AuditKafkaEventsResponseDto(
        String topic,
        int count,
        List<AuditKafkaRecordDto> items
) {
}

