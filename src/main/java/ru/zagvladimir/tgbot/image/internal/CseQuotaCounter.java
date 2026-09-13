package ru.zagvladimir.tgbot.image.internal;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class CseQuotaCounter {

    private final JdbcClient jdbcClient;
    private final Clock clock;

    CseQuotaCounter(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }

    @Transactional
    boolean tryConsume(int dailyLimit) {
        var today = LocalDate.now(clock.withZone(ZoneOffset.UTC));

        var consumed = jdbcClient
                .sql(
                        """
                        insert into cse_quota (day, used) values (?, 1)
                        on conflict (day) do update set used = cse_quota.used + 1
                        where cse_quota.used < ?
                        returning used
                        """)
                .param(today)
                .param(dailyLimit)
                .query(Integer.class)
                .optional();

        return consumed.isPresent();
    }

    @Transactional(readOnly = true)
    int usedToday() {
        var today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        return jdbcClient
                .sql("select used from cse_quota where day = ?")
                .param(today)
                .query(Integer.class)
                .optional()
                .orElse(0);
    }
}
