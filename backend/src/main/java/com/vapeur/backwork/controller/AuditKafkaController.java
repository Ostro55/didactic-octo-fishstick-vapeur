package com.vapeur.backwork.controller;

import com.vapeur.backwork.audit.KafkaAuditEventReader;
import com.vapeur.backwork.dto.AuditKafkaEventsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "audit.kafka.enabled", havingValue = "true")
@RequestMapping("admin/audit")
public class AuditKafkaController {

    private final KafkaAuditEventReader reader;

    @GetMapping("events")
    public ResponseEntity<?> getLastEvents(@RequestParam(defaultValue = "50") int limit) {
        if (limit < 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "limit must be >= 1"));
        }
        try {
            AuditKafkaEventsResponseDto out = reader.readLastEvents(limit);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Kafka is unavailable", "details", e.getClass().getSimpleName()));
        }
    }
}

