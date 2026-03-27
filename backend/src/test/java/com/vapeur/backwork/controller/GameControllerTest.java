package com.vapeur.backwork.controller;

import com.vapeur.backwork.utils.GameGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
