package ru.zagvladimir.tgbot.infra.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.subscription.model.Subscription;
import ru.zagvladimir.tgbot.subscription.model.SubscriptionType;
import ru.zagvladimir.tgbot.subscription.port.SubscriptionRepository;

@Repository
public class JdbcSubscriptionRepository implements SubscriptionRepository {

    private final JdbcClient jdbcClient;

    JdbcSubscriptionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    @Transactional
    public Subscription create(
            long chatId,
            long userId,
            SubscriptionType type,
            String payload,
            String cron,
            ZoneId zoneId,
            Instant nextRunAt) {
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
                .param(OffsetDateTime.ofInstant(nextRunAt, ZoneOffset.UTC))
                .param(userId)
                .query(Long.class)
                .single();

        return new Subscription(id, chatId, type, payload, cron, zoneId, nextRunAt, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findByChat(long chatId) {
        return jdbcClient
                .sql("select * from subscription where chat_id = ? order by id")
                .param(chatId)
                .query(JdbcSubscriptionRepository::toSubscription)
                .list();
    }

    @Override
    @Transactional
    public boolean delete(long chatId, long subscriptionId) {
        return jdbcClient
                        .sql("delete from subscription where chat_id = ? and id = ?")
                        .param(chatId)
                        .param(subscriptionId)
                        .update()
                > 0;
    }

    @Override
    @Transactional
    public List<Subscription> lockDue(Instant now, int limit) {
        return jdbcClient
                .sql(
                        """
                        select * from subscription
                        where enabled and next_run_at <= ?
                        order by next_run_at
                        for update skip locked
                        limit ?
                        """)
                .param(OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .param(limit)
                .query(JdbcSubscriptionRepository::toSubscription)
                .list();
    }

    @Override
    @Transactional
    public void reschedule(long subscriptionId, Instant nextRunAt, Instant lastRunAt) {
        jdbcClient
                .sql("update subscription set next_run_at = ?, last_run_at = ? where id = ?")
                .param(OffsetDateTime.ofInstant(nextRunAt, ZoneOffset.UTC))
                .param(OffsetDateTime.ofInstant(lastRunAt, ZoneOffset.UTC))
                .param(subscriptionId)
                .update();
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
