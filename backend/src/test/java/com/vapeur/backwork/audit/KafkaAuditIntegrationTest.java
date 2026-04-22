package com.vapeur.backwork.audit;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.UserRepository;
import com.vapeur.backwork.service.UserService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=h2,kafka",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "audit.topic=audit-events",
        "audit.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:kafkatest;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"audit-events"})
@DirtiesContext
@EnabledIfSystemProperty(named = "it.kafka.embedded", matches = "true")
class KafkaAuditIntegrationTest {

    private static final String TOPIC = "audit-events";

    @Autowired
    MockMvc mvc;

    @Autowired
    EmbeddedKafkaBroker broker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    AuditEventPublisher auditEventPublisher;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close(Duration.ofSeconds(1));
    }

    @Test
    void createUser_emitsAuditEvent() throws Exception {
        assertTrue(auditEventPublisher instanceof KafkaAuditEventPublisher, "Expected KafkaAuditEventPublisher, got " + auditEventPublisher.getClass().getName());

        consumer = newConsumer();
        consumer.subscribe(List.of(TOPIC));

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(userJson("alice", "alice@example.com", false)))
                .andExpect(status().isCreated());

        JsonNode event = awaitEventJson(consumer, Duration.ofSeconds(15), "USER_CREATED", Map.of("username", "alice"));
        assertEquals("SYSTEM", event.path("actorType").asText());
        assertEquals("USER_CREATED", event.path("action").asText());
        assertEquals("USER", event.path("resourceType").asText());
        assertNotNull(event.path("eventId").asText(null));
        assertFalse(event.path("eventId").asText().isBlank());
        assertFalse(event.path("occurredAt").asText().isBlank());
        assertEquals("alice", event.path("metadata").path("username").asText());
    }

    @Test
    void submitGame_emitsAuditEvent_withUserActor_andDerivedStatus() throws Exception {
        assertTrue(auditEventPublisher instanceof KafkaAuditEventPublisher, "Expected KafkaAuditEventPublisher, got " + auditEventPublisher.getClass().getName());

        long userId = userRepository.save(user("bob", "bob@example.com", false)).getId();

        consumer = newConsumer();
        consumer.subscribe(List.of(TOPIC));

        mvc.perform(post("/games/save")
                        .param("userId", String.valueOf(userId))
                        .contentType("application/json")
                        .content(gameJson("Hades", 10)))
                .andExpect(status().isCreated());

        JsonNode event = awaitEventJson(consumer, Duration.ofSeconds(15), "GAME_SUBMITTED", Map.of("name", "Hades"));
        assertEquals("USER", event.path("actorType").asText());
        assertEquals("bob", event.path("actorUsername").asText());
        assertEquals(false, event.path("actorAdmin").asBoolean());
        assertEquals("GAME", event.path("resourceType").asText());
        assertEquals("Hades", event.path("metadata").path("name").asText());
        assertEquals("pending", event.path("metadata").path("status").asText());
    }

    @Test
    void transactionalUpdateUser_emitsOnlyAfterCommit() throws Exception {
        assertTrue(auditEventPublisher instanceof KafkaAuditEventPublisher, "Expected KafkaAuditEventPublisher, got " + auditEventPublisher.getClass().getName());

        User existing = userRepository.save(user("carol", "carol@example.com", false));

        consumer = newConsumer();
        consumer.subscribe(List.of(TOPIC));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            User updated = new User();
            updated.setUsername("carol2");
            updated.setPassword("ignored");
            updated.setEmail("carol2@example.com");
            updated.setAdmin(true);

            userService.updateUser(existing.getId(), updated);

            // Still inside transaction: publisher registers afterCommit callback.
            assertTrue(drainRecords(consumer, Duration.ofMillis(200)).isEmpty());
        });

        JsonNode event = awaitEventJson(consumer, Duration.ofSeconds(15), "USER_UPDATED", Map.of("username", "carol2"));
        assertEquals("USER_UPDATED", event.path("action").asText());
        assertEquals("carol2", event.path("metadata").path("username").asText());
        assertEquals(true, event.path("metadata").path("isAdmin").asBoolean());
    }

    private Consumer<String, String> newConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("test-" + UUID.randomUUID(), "false", broker);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    private JsonNode awaitEventJson(
            Consumer<String, String> c,
            Duration timeout,
            String expectedAction,
            Map<String, String> expectedMetadata
    ) throws Exception {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();
        List<ConsumerRecord<String, String>> seen = new ArrayList<>();
        while (System.nanoTime() < deadlineNanos) {
            seen.addAll(drainRecords(c, Duration.ofMillis(250)));
            for (ConsumerRecord<String, String> r : seen) {
                JsonNode node = objectMapper.readTree(r.value());
                if (!expectedAction.equals(node.path("action").asText())) continue;
                boolean metaOk = true;
                for (Map.Entry<String, String> e : expectedMetadata.entrySet()) {
                    if (!e.getValue().equals(node.path("metadata").path(e.getKey()).asText())) {
                        metaOk = false;
                        break;
                    }
                }
                if (metaOk) return node;
            }
        }
        throw new AssertionError("No audit events received from Kafka within timeout; expected action=" + expectedAction + " metadata=" + expectedMetadata + " (seen=" + seen.size() + ")");
    }

    private List<ConsumerRecord<String, String>> drainRecords(Consumer<String, String> c, Duration poll) {
        ConsumerRecords<String, String> recs = c.poll(poll);
        List<ConsumerRecord<String, String>> out = new ArrayList<>();
        for (ConsumerRecord<String, String> r : recs) out.add(r);
        return out;
    }

    private static String userJson(String username, String email, boolean isAdmin) {
        return "{"
                + "\"username\":" + jsonString(username)
                + ",\"password\":" + jsonString("pw")
                + ",\"email\":" + jsonString(email)
                + ",\"isAdmin\":" + isAdmin
                + ",\"recommendedGames\":[]"
                + "}";
    }

    private static String gameJson(String name, long price) {
        return "{"
                + "\"name\":" + jsonString(name)
                + ",\"price\":" + price
                + ",\"genre\":[\"action\"]"
                + "}";
    }

    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static User user(String username, String email, boolean admin) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("pw");
        u.setEmail(email);
        u.setAdmin(admin);
        return u;
    }
}