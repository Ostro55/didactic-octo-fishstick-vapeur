package com.vapeur.backwork.controller;

import com.vapeur.backwork.utils.GameGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=h2",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    MockMvc mvc;

    @BeforeEach
    void cleanDb() throws Exception {
        mvc.perform(delete("/games"))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/users"))
                .andExpect(status().isNoContent());
    }

    @Test
    void save_thenGetAll_roundTripGenreEnumSet() throws Exception {
        String payload = gameJson("Doom", 10, GameGenre.action.name(), GameGenre.horror.name());

        mvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Doom"))
                .andExpect(jsonPath("$.price").value(10))
                .andExpect(jsonPath("$.genre", containsInAnyOrder("action", "horror")));

        mvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Doom"))
                .andExpect(jsonPath("$[0].genre", containsInAnyOrder("action", "horror")));
    }

    @Test
    void getAll_withGenreFilter_isCaseInsensitive() throws Exception {
        mvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("A", 1, GameGenre.action.name())))
                .andExpect(status().isCreated());

        mvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("B", 1, GameGenre.romance.name())))
                .andExpect(status().isCreated());

        mvc.perform(get("/games").param("genre", "AcTiOn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void getAll_withInvalidGenre_returnsEmptyArray() throws Exception {
        mvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("A", 1, GameGenre.action.name())))
                .andExpect(status().isCreated());

        mvc.perform(get("/games").param("genre", "not-a-genre"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void clean_clearsGames() throws Exception {
        mvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("A", 1, GameGenre.action.name())))
                .andExpect(status().isCreated());

        mvc.perform(delete("/games"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void saveWithUser_admin_setsAccepted() throws Exception {
        long adminId = createUser("admin", "admin@example.com", true);

        mvc.perform(post("/games/save").param("userId", String.valueOf(adminId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Doom", 10, GameGenre.action.name())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("accepted"));
    }

    @Test
    void saveWithUser_nonAdmin_setsPending() throws Exception {
        long userId = createUser("user", "user@example.com", false);

        mvc.perform(post("/games/save").param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Hades", 10, GameGenre.action.name())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void saveWithUser_unknownUser_returns404() throws Exception {
        mvc.perform(post("/games/save").param("userId", "999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameJson("Celeste", 10, GameGenre.action.name())))
                .andExpect(status().isNotFound());
    }

    private long createUser(String username, String email, boolean isAdmin) throws Exception {
        MvcResult create = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, email, isAdmin)))
                .andExpect(status().isCreated())
                .andReturn();

        Number userIdNum = com.jayway.jsonpath.JsonPath.read(create.getResponse().getContentAsString(), "$.id");
        return userIdNum.longValue();
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

    private static String gameJson(String name, long price, String... genres) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":").append(jsonString(name));
        sb.append(",\"price\":").append(price);
        sb.append(",\"genre\":[");
        for (int i = 0; i < genres.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(jsonString(genres[i]));
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String jsonString(String s) {
        // Minimal escaping for test payloads.
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
