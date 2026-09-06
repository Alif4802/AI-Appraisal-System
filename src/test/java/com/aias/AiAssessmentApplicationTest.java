package com.aias;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Non-database application context smoke test.
 * Verifies Spring Boot foundation loads without requiring an external database or Docker.
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration,"
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceInitializationAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
})
@ActiveProfiles("test")
class AiAssessmentApplicationTest {

    @Test
    void contextLoads() {
        // Verifies Spring context initializes successfully
    }
}
