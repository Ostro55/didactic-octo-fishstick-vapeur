package com.vapeur.backwork.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=h2,kafka",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "audit.topic=audit-events",
        "audit.kafka.enabled=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:kafkatest_endpoint;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"audit-events"})
@DirtiesContext
@EnabledIfSystemProperty(named = "it.kafka.embedded", matches = "true")
class KafkaAuditEventsEndpointIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void endpoint_readsLastEvents() throws Exception {
        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\"username\":\"alice\",\"password\":\"pw\",\"email\":\"alice@example.com\",\"isAdmin\":false,\"recommendedGames\":[]}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/admin/audit/events").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("audit-events"))
                .andExpect(jsonPath("$.count", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.items[0].event.action").exists());
    }
}
