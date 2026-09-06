package com.aias;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostgreSQL Integration Test using Testcontainers 2.x and Spring Boot @ServiceConnection.
 * Verifies Flyway migration startup and database connectivity against real PostgreSQL.
 *
 * If Docker is unavailable on the workstation, this test is cleanly disabled/skipped.
 */
@SpringBootTest(properties = {
    "spring.flyway.enabled=true"
})
@Testcontainers(disabledWithoutDocker = true)
@EnabledIf("isDockerAvailable")
@ActiveProfiles("test")
class DatabaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    void databaseAndFlywayStartupVerification() throws SQLException {
        // 1. PostgreSQL Testcontainer starts
        assertThat(postgres.isRunning()).isTrue();

        // 2. Spring datasource is connected to that container
        assertThat(dataSource).isNotNull();
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).containsIgnoringCase("PostgreSQL");
        }

        // 3. Flyway auto-configuration is active and bean exists
        assertThat(flyway).isNotNull();

        // 4. Verification that Flyway initialized with zero business/domain migrations in Phase 0
        assertThat(flyway.info().applied()).isEmpty();
        assertThat(flyway.info().pending()).isEmpty();
    }
}
