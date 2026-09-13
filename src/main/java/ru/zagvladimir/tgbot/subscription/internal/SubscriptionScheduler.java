package ru.zagvladimir.tgbot.subscription.internal;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.subscription.Subscription;
import ru.zagvladimir.tgbot.subscription.SubscriptionDue;

@Component
class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);
    private static final int BATCH_SIZE = 50;

    private final JdbcClient jdbcClient;
    private final ApplicationEventPublisher events;
    private final java.time.Clock clock;

    SubscriptionScheduler(JdbcClient jdbcClient, ApplicationEventPublisher events, java.time.Clock clock) {
        this.jdbcClient = jdbcClient;
        this.events = events;
        this.clock = clock;
    }

    @Scheduled(initialDelayString = "PT15S", fixedDelayString = "PT30S")
    void tick() {
        var fired = fireDueSubscriptions();
        if (fired > 0) {
            log.info("Отправлено в рассылку подписок: {}", fired);
        }
    }

    @Transactional
    int fireDueSubscriptions() {
        var now = Instant.now(clock);
        var due = selectDue(now);

        for (var subscription : due) {
            var nextRun = JdbcSubscriptionService.nextRunAfter(subscription.cron(), subscription.zoneId(), now);

            jdbcClient
                    .sql("update subscription set next_run_at = ?, last_run_at = ? where id = ?")
                    .param(OffsetDateTime.ofInstant(nextRun, ZoneOffset.UTC))
                    .param(OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                    .param(subscription.id())
                    .update();

            events.publishEvent(new SubscriptionDue(
                    subscription.id(), subscription.chatId(), subscription.type(), subscription.payload()));
        }

        return due.size();
    }

    private List<Subscription> selectDue(Instant now) {
        return jdbcClient
                .sql(
                        """
                        select * from subscription
                        where enabled and next_run_at <= ?
                        order by next_run_at
                        for update skip locked
                        limit %d
                        """
                                .formatted(BATCH_SIZE))
                .param(OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .query(JdbcSubscriptionService::toSubscription)
                .list();
    }
}
