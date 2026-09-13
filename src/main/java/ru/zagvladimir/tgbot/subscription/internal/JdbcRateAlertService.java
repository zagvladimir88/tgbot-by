package ru.zagvladimir.tgbot.subscription.internal;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.subscription.RateAlert;
import ru.zagvladimir.tgbot.subscription.RateAlertCondition;
import ru.zagvladimir.tgbot.subscription.RateAlertService;

@Service
class JdbcRateAlertService implements RateAlertService {

    private final JdbcClient jdbcClient;

    JdbcRateAlertService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    @Transactional
    public RateAlert create(
            long chatId,
            long userId,
            String currency,
            RateAlertCondition condition,
            BigDecimal threshold,
            boolean oneShot) {
        var code = currency.toUpperCase(Locale.ROOT);

        var id = jdbcClient
                .sql(
                        """
                        insert into rate_alert (chat_id, currency, condition, threshold, one_shot, created_by)
                        values (?, ?, ?, ?, ?, ?)
                        returning id
                        """)
                .param(chatId)
                .param(code)
                .param(condition.name())
                .param(threshold)
                .param(oneShot)
                .param(userId)
                .query(Long.class)
                .single();

        return new RateAlert(id, chatId, code, condition, threshold, oneShot, true, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RateAlert> activeAlerts() {
        return jdbcClient
                .sql("select * from rate_alert where enabled order by id")
                .query(JdbcRateAlertService::toAlert)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RateAlert> forChat(long chatId) {
        return jdbcClient
                .sql("select * from rate_alert where chat_id = ? order by id")
                .param(chatId)
                .query(JdbcRateAlertService::toAlert)
                .list();
    }

    @Override
    @Transactional
    public boolean remove(long chatId, long alertId) {
        return jdbcClient
                        .sql("delete from rate_alert where chat_id = ? and id = ?")
                        .param(chatId)
                        .param(alertId)
                        .update()
                > 0;
    }

    @Override
    @Transactional
    public void markFired(long alertId, LocalDate onDate) {
        jdbcClient
                .sql(
                        """
                        update rate_alert
                        set last_fired_on_date = ?, enabled = case when one_shot then false else enabled end
                        where id = ?
                        """)
                .param(onDate)
                .param(alertId)
                .update();
    }

    static RateAlert toAlert(ResultSet rs, int rowNum) throws SQLException {
        return new RateAlert(
                rs.getLong("id"),
                rs.getLong("chat_id"),
                rs.getString("currency"),
                RateAlertCondition.valueOf(rs.getString("condition")),
                rs.getBigDecimal("threshold"),
                rs.getBoolean("one_shot"),
                rs.getBoolean("enabled"),
                rs.getObject("last_fired_on_date", LocalDate.class));
    }
}
