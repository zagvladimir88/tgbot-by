package ru.zagvladimir.tgbot.infra.persistence;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.domain.image.port.SearchQuotaPort;

@Component
public class CseQuotaCounter implements SearchQuotaPort {

    private final JdbcClient jdbcClient;
    private final Clock clock;

    CseQuotaCounter(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }

    @Override
    @Transactional
    public boolean tryConsume(int dailyLimit) {
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

    @Override
    @Transactional(readOnly = true)
    public int usedToday() {
        var today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        return jdbcClient
                .sql("select used from cse_quota where day = ?")
                .param(today)
                .query(Integer.class)
                .optional()
                .orElse(0);
    }
}
