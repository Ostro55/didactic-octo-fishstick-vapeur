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
        mvc.perform(delete("/users"))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/games"))
                .andExpect(status().isNoContent());
    }

    @Test
    void save_thenGetAll_thenGetById() throws Exception {
        MvcResult create = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("alice", "alice@example.com", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.recommendedGames", hasSize(0)))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].isAdmin").value(false))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        Number userIdNum = com.jayway.jsonpath.JsonPath.read(create.getResponse().getContentAsString(), "$.id");
        long userId = userIdNum.longValue();

        mvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void update_thenDelete() throws Exception {
        MvcResult create = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("bob", "bob@example.com", false)))
                .andExpect(status().isCreated())
                .andReturn();

        Number userIdNum = com.jayway.jsonpath.JsonPath.read(create.getResponse().getContentAsString(), "$.id");
        long userId = userIdNum.longValue();

        mvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("bob", "bob2@example.com", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob2@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(delete("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void login_doesNotExposePassword() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("charlie", "charlie@example.com", false)))
                .andExpect(status().isCreated());

        mvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("charlie@example.com", "pw")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("charlie"))
                .andExpect(jsonPath("$.email").value("charlie@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
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

    private static String loginJson(String email, String password) {
        return "{"
                + "\"email\":" + jsonString(email)
                + ",\"password\":" + jsonString(password)
                + "}";
    }

    private static String jsonString(String s) {
        // Minimal escaping for test payloads.
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
