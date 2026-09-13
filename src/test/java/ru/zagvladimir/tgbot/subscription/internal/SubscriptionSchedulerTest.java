package ru.zagvladimir.tgbot.subscription.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;
import ru.zagvladimir.tgbot.subscription.SubscriptionService;
import ru.zagvladimir.tgbot.subscription.SubscriptionType;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SubscriptionSchedulerTest {

    private static final long CHAT_ID = 900L;
    private static final ZoneId MINSK = ZoneId.of("Europe/Minsk");

    @Autowired
    private SubscriptionService subscriptions;

    @Autowired
    private SubscriptionScheduler scheduler;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void clean() {
        jdbcClient.sql("delete from subscription").update();
        jdbcClient.sql("delete from event_publication").update();
    }

    @Test
    void movesNextRunForwardAfterFiring() {
        var subscription = subscriptions.subscribe(CHAT_ID, 1L, SubscriptionType.RATES, "USD", "0 0 10 * * *", MINSK);
        makeDue(subscription.id());

        var fired = scheduler.fireDueSubscriptions();

        assertThat(fired).isEqualTo(1);
        assertThat(nextRunOf(subscription.id())).isAfter(Instant.now());
        assertThat(lastRunOf(subscription.id())).isNotNull();
    }

    @Test
    void firesEachDueSubscriptionExactlyOnce() {
        var subscription = subscriptions.subscribe(CHAT_ID, 1L, SubscriptionType.RATES, "", "0 0 10 * * *", MINSK);
        makeDue(subscription.id());

        assertThat(scheduler.fireDueSubscriptions()).isEqualTo(1);
        assertThat(scheduler.fireDueSubscriptions()).isZero();
    }

    @Test
    void ignoresDisabledSubscriptions() {
        var subscription =
                subscriptions.subscribe(CHAT_ID, 1L, SubscriptionType.WEATHER, "Минск", "0 0 9 * * *", MINSK);
        makeDue(subscription.id());
        jdbcClient
                .sql("update subscription set enabled = false where id = ?")
                .param(subscription.id())
                .update();

        assertThat(scheduler.fireDueSubscriptions()).isZero();
    }

    @Test
    void writesEventIntoPublicationRegistry() {
        var subscription = subscriptions.subscribe(CHAT_ID, 1L, SubscriptionType.RATES, "", "0 0 10 * * *", MINSK);
        makeDue(subscription.id());

        scheduler.fireDueSubscriptions();

        var publications = jdbcClient
                .sql("select count(*) from event_publication")
                .query(Integer.class)
                .single();

        assertThat(publications).isPositive();
    }

    @Test
    void computesNextRunInSubscriptionTimezoneNotServerOne() {
        var tokyo = ZoneId.of("Asia/Tokyo");
        var from = Instant.parse("2026-09-13T00:00:00Z");

        var minskRun = JdbcSubscriptionService.nextRunAfter("0 0 9 * * *", MINSK, from);
        var tokyoRun = JdbcSubscriptionService.nextRunAfter("0 0 9 * * *", tokyo, from);

        assertThat(minskRun).isNotEqualTo(tokyoRun);
        assertThat(minskRun.atZone(MINSK).getHour()).isEqualTo(9);
        assertThat(tokyoRun.atZone(tokyo).getHour()).isEqualTo(9);
    }

    @Test
    void survivesDaylightSavingTransition() {
        var warsaw = ZoneId.of("Europe/Warsaw");
        var beforeSpringForward =
                LocalDateTime.of(2026, 3, 28, 12, 0).atZone(warsaw).toInstant();

        var next = JdbcSubscriptionService.nextRunAfter("0 30 2 * * *", warsaw, beforeSpringForward);

        assertThat(next).isNotNull();
        assertThat(next).isAfter(beforeSpringForward);
    }

    private void makeDue(long id) {
        jdbcClient
                .sql("update subscription set next_run_at = ? where id = ?")
                .param(OffsetDateTime.ofInstant(Instant.now().minusSeconds(60), ZoneOffset.UTC))
                .param(id)
                .update();
    }

    private Instant nextRunOf(long id) {
        return jdbcClient
                .sql("select next_run_at from subscription where id = ?")
                .param(id)
                .query(OffsetDateTime.class)
                .single()
                .toInstant();
    }

    private OffsetDateTime lastRunOf(long id) {
        return jdbcClient
                .sql("select last_run_at from subscription where id = ?")
                .param(id)
                .query(OffsetDateTime.class)
                .single();
    }
}
