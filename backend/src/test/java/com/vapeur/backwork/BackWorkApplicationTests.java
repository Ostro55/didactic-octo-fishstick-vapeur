package com.vapeur.backwork;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.profiles.active=h2",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BackWorkApplicationTests {

    @Test
    void contextLoads() {
    }

}
