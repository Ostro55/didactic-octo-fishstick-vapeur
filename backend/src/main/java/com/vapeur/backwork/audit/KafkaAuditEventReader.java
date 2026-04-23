package com.vapeur.backwork.audit;

import com.vapeur.backwork.dto.AuditKafkaEventsResponseDto;
import com.vapeur.backwork.dto.AuditKafkaRecordDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "audit.kafka.enabled", havingValue = "true")
public class KafkaAuditEventReader {

    private static final int DEFAULT_MAX_LIMIT = 500;

    private final ObjectMapper objectMapper;

    @Value("${audit.topic:audit-events}")
    private String topic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public AuditKafkaEventsResponseDto readLastEvents(int limit) {
        return readLastEvents(limit, Duration.ofSeconds(2));
    }

    public AuditKafkaEventsResponseDto readLastEvents(int limit, Duration timeout) {
        int capped = Math.max(1, Math.min(DEFAULT_MAX_LIMIT, limit));
        List<AuditKafkaRecordDto> records = new ArrayList<>();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "audit-read-" + UUID.randomUUID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                    .map(p -> new TopicPartition(topic, p.partition()))
                    .toList();

            if (partitions.isEmpty()) {
                return new AuditKafkaEventsResponseDto(topic, 0, List.of());
            }

            consumer.assign(partitions);

            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            for (TopicPartition tp : partitions) {
                long end = endOffsets.getOrDefault(tp, 0L);
                long start = Math.max(0L, end - capped);
                consumer.seek(tp, start);
            }

            long deadlineNanos = System.nanoTime() + timeout.toNanos();
            int emptyPolls = 0;
            int maxToRead = partitions.size() * capped;
            while (System.nanoTime() < deadlineNanos && emptyPolls < 3 && records.size() < maxToRead) {
                ConsumerRecords<String, String> polled = consumer.poll(Duration.ofMillis(200));
                if (polled.isEmpty()) {
                    emptyPolls++;
                    continue;
                }
                emptyPolls = 0;
                for (ConsumerRecord<String, String> r : polled) {
                    records.add(toDto(r));
                }
            }
        }

        // "Last N" across partitions: order by (timestamp, partition, offset), then keep newest.
        records.sort(Comparator
                .comparingLong(AuditKafkaRecordDto::timestamp)
                .thenComparingInt(AuditKafkaRecordDto::partition)
                .thenComparingLong(AuditKafkaRecordDto::offset));

        int from = Math.max(0, records.size() - capped);
        List<AuditKafkaRecordDto> last = records.subList(from, records.size());
        List<AuditKafkaRecordDto> newestFirst = new ArrayList<>(last);
        newestFirst.sort(Comparator
                .comparingLong(AuditKafkaRecordDto::timestamp).reversed()
                .thenComparingInt(AuditKafkaRecordDto::partition).reversed()
                .thenComparingLong(AuditKafkaRecordDto::offset).reversed());

        return new AuditKafkaEventsResponseDto(topic, newestFirst.size(), newestFirst);
    }

    private AuditKafkaRecordDto toDto(ConsumerRecord<String, String> r) {
        String value = r.value();
        JsonNode event = null;
        String raw = null;
        if (value != null) {
            try {
                event = objectMapper.readTree(value);
            } catch (Exception e) {
                raw = value;
            }
        }
        return new AuditKafkaRecordDto(
                r.partition(),
                r.offset(),
                r.timestamp(),
                r.key(),
                event,
                raw
        );
    }
}
