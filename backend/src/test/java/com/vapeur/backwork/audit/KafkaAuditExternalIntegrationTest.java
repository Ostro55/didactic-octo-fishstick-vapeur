package com.vapeur.backwork.audit;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.vapeur.backwork.entity.User;
import com.vapeur.backwork.repository.UserRepository;
import com.vapeur.backwork.service.UserService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test using an external Kafka broker (e.g. docker-compose localhost:9092).
 * This avoids @EmbeddedKafka which may be blocked in restricted environments.
 */
@SpringBootTest(properties = {
        "spring.profiles.active=h2,kafka",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "audit.topic=audit-events",
        "audit.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}"
})
@AutoConfigureMockMvc
@DirtiesContext
@EnabledIfSystemProperty(named = "it.kafka.external", matches = "true")
class KafkaAuditExternalIntegrationTest {

    private static final String TOPIC = "audit-events";

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    AuditEventPublisher auditEventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<String, String> consumer;
    private String bootstrap;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close(Duration.ofSeconds(1));
    }

    @BeforeEach
    void waitForKafka() throws Exception {
        this.bootstrap = System.getProperty("kafka.bootstrap", System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        waitForKafkaReady(bootstrap, Duration.ofSeconds(20));
    }

    @Test
    void createUser_emitsAuditEvent() throws Exception {
        assertTrue(auditEventPublisher instanceof KafkaAuditEventPublisher, "Expected KafkaAuditEventPublisher, got " + auditEventPublisher.getClass().getName());

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "alice-" + suffix;
        String email = "alice-" + suffix + "@example.com";

        consumer = newConsumer();
        consumer.subscribe(List.of(TOPIC));

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(userJson(username, email, false)))
                .andExpect(status().isCreated());

        JsonNode event = awaitEventJson(consumer, Duration.ofSeconds(20), "USER_CREATED", Map.of("username", username));
        assertEquals("USER_CREATED", event.path("action").asText());
        assertEquals(username, event.path("metadata").path("username").asText());
    }

    @Test
    void submitGame_emitsAuditEvent() throws Exception {
        assertTrue(auditEventPublisher instanceof KafkaAuditEventPublisher, "Expected KafkaAuditEventPublisher, got " + auditEventPublisher.getClass().getName());

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        long userId = userRepository.save(user("bob-" + suffix, "bob-" + suffix + "@example.com", false)).getId();
        String gameName = "Hades-" + suffix;

        consumer = newConsumer();
        consumer.subscribe(List.of(TOPIC));

        mvc.perform(post("/games/save")
                        .param("userId", String.valueOf(userId))
                        .contentType("application/json")
                        .content(gameJson(gameName, 10)))
                .andExpect(status().isCreated());

        JsonNode event = awaitEventJson(consumer, Duration.ofSeconds(20), "GAME_SUBMITTED", Map.of("name", gameName));
        assertEquals("GAME_SUBMITTED", event.path("action").asText());
        assertEquals(gameName, event.path("metadata").path("name").asText());
        assertEquals("pending", event.path("metadata").path("status").asText());
    }

    @Test
    void transactionalUpdateUser_emitsOnlyAfterCommit() throws Exception {
        assertTrue(auditEventPublisher instanceof KafkaAuditEventPublisher, "Expected KafkaAuditEventPublisher, got " + auditEventPublisher.getClass().getName());

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User existing = userRepository.save(user("carol-" + suffix, "carol-" + suffix + "@example.com", false));

        consumer = newConsumer();
        consumer.subscribe(List.of(TOPIC));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            User updated = new User();
            updated.setUsername("carol2-" + suffix);
            updated.setPassword("ignored");
            updated.setEmail("carol2-" + suffix + "@example.com");
            updated.setAdmin(true);

            userService.updateUser(existing.getId(), updated);
        });

        JsonNode event = awaitEventJson(consumer, Duration.ofSeconds(20), "USER_UPDATED", Map.of("username", "carol2-" + suffix));
        assertEquals("USER_UPDATED", event.path("action").asText());
        assertEquals("carol2-" + suffix, event.path("metadata").path("username").asText());
        assertEquals(true, event.path("metadata").path("isAdmin").asBoolean());
    }

    private Consumer<String, String> newConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // External broker topics can be persistent; read from earliest and filter using unique metadata.
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

    private static void waitForKafkaReady(String bootstrap, Duration timeout) throws Exception {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();
        Exception last = null;
        while (System.nanoTime() < deadlineNanos) {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
            try (AdminClient admin = AdminClient.create(props)) {
                // Any successful metadata call is enough.
                admin.listTopics().names().get(2, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                last = e;
                Thread.sleep(300);
            }
        }
        throw new AssertionError("Kafka not ready at " + bootstrap + " within " + timeout + (last == null ? "" : (": " + last)));
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
