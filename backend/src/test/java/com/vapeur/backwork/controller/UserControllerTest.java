package com.vapeur.backwork.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=h2",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void cleanDb() throws Exception {
        mvc.perform(delete("/user/clean"))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/game/clean"))
                .andExpect(status().isNoContent());
    }

    @Test
    void save_thenGetAll_thenGetById() throws Exception {
        MvcResult create = mvc.perform(post("/user/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("alice", "alice@example.com", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.recommendedGames", hasSize(0)))
                .andReturn();

        mvc.perform(get("/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].isAdmin").value(false));

        Number userIdNum = com.jayway.jsonpath.JsonPath.read(create.getResponse().getContentAsString(), "$.id");
        long userId = userIdNum.longValue();

        mvc.perform(get("/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void update_thenDelete() throws Exception {
        MvcResult create = mvc.perform(post("/user/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("bob", "bob@example.com", false)))
                .andExpect(status().isCreated())
                .andReturn();

        Number userIdNum = com.jayway.jsonpath.JsonPath.read(create.getResponse().getContentAsString(), "$.id");
        long userId = userIdNum.longValue();

        mvc.perform(put("/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("bob", "bob2@example.com", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob2@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(true));

        mvc.perform(delete("/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));

        mvc.perform(get("/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private static String userJson(String username, String email, boolean isAdmin) {
        return "{"
                + "\"username\":" + jsonString(username)
                + ",\"email\":" + jsonString(email)
                + ",\"isAdmin\":" + isAdmin
                + ",\"recommendedGames\":[]"
                + "}";
    }

    private static String jsonString(String s) {
        // Minimal escaping for test payloads.
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
