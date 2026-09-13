package ru.zagvladimir.tgbot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseMigrationTest {

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void flywayAppliesMigrations() {
        var applied = jdbcClient
                .sql("select count(*) from flyway_schema_history where success = true")
                .query(Integer.class)
                .single();

        assertThat(applied).isPositive();
    }

    @Test
    void chatSettingsTableIsCreatedWithDefaults() {
        jdbcClient
                .sql("insert into chat_settings (chat_id) values (?)")
                .param(42L)
                .update();

        var zoneId = jdbcClient
                .sql("select zone_id from chat_settings where chat_id = ?")
                .param(42L)
                .query(String.class)
                .single();

        assertThat(zoneId).isEqualTo("Europe/Minsk");
    }

    @Test
    void modulithEventPublicationTableExists() {
        var exists = jdbcClient
                .sql("select exists (select from information_schema.tables where table_name = 'event_publication')")
                .query(Boolean.class)
                .single();

        assertThat(exists).isTrue();
    }
}
