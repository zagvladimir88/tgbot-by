package ru.zagvladimir.tgbot.subscription.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.subscription.Subscription;
import ru.zagvladimir.tgbot.subscription.SubscriptionService;
import ru.zagvladimir.tgbot.subscription.SubscriptionType;

@Service
class JdbcSubscriptionService implements SubscriptionService {

    private final JdbcClient jdbcClient;
    private final Clock clock;

    JdbcSubscriptionService(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Subscription subscribe(
            long chatId, long userId, SubscriptionType type, String payload, String cron, ZoneId zoneId) {
        var nextRun = nextRunAfter(cron, zoneId, Instant.now(clock));

        var id = jdbcClient
                .sql(
                        """
                        insert into subscription (chat_id, type, payload, cron, zone_id, next_run_at, created_by)
                        values (?, ?, ?, ?, ?, ?, ?)
                        returning id
                        """)
                .param(chatId)
                .param(type.name())
                .param(payload)
                .param(cron)
                .param(zoneId.getId())
                .param(OffsetDateTime.ofInstant(nextRun, ZoneOffset.UTC))
                .param(userId)
                .query(Long.class)
                .single();

        return new Subscription(id, chatId, type, payload, cron, zoneId, nextRun, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> forChat(long chatId) {
        return jdbcClient
                .sql("select * from subscription where chat_id = ? order by id")
                .param(chatId)
                .query(JdbcSubscriptionService::toSubscription)
                .list();
    }

    @Override
    @Transactional
    public boolean unsubscribe(long chatId, long subscriptionId) {
        return jdbcClient
                        .sql("delete from subscription where chat_id = ? and id = ?")
                        .param(chatId)
                        .param(subscriptionId)
                        .update()
                > 0;
    }

    static Instant nextRunAfter(String cron, ZoneId zoneId, Instant from) {
        var next = CronExpression.parse(cron).next(from.atZone(zoneId));
        if (next == null) {
            throw new IllegalArgumentException("Расписание %s больше никогда не сработает".formatted(cron));
        }
        return next.toInstant();
    }

    static Subscription toSubscription(ResultSet rs, int rowNum) throws SQLException {
        var lastRun = rs.getObject("last_run_at", OffsetDateTime.class);

        return new Subscription(
                rs.getLong("id"),
                rs.getLong("chat_id"),
                SubscriptionType.valueOf(rs.getString("type")),
                rs.getString("payload"),
                rs.getString("cron"),
                ZoneId.of(rs.getString("zone_id")),
                rs.getObject("next_run_at", OffsetDateTime.class).toInstant(),
                lastRun == null ? null : lastRun.toInstant(),
                rs.getBoolean("enabled"));
    }
}
