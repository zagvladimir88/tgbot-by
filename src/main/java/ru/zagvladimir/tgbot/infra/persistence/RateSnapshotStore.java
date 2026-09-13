package ru.zagvladimir.tgbot.infra.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.domain.currency.model.CurrencyRate;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.domain.currency.port.RateSnapshotRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

@Component
public class RateSnapshotStore implements RateSnapshotRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    RateSnapshotStore(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(RateSnapshot snapshot) {
        var payload = objectMapper.writeValueAsString(StoredRate.from(snapshot));

        jdbcClient
                .sql(
                        """
                        insert into rate_snapshot (on_date, fetched_at, payload)
                        values (?, ?, cast(? as jsonb))
                        on conflict (on_date) do update
                        set fetched_at = excluded.fetched_at, payload = excluded.payload
                        """)
                .param(snapshot.onDate())
                .param(OffsetDateTime.ofInstant(snapshot.fetchedAt(), ZoneOffset.UTC))
                .param(payload)
                .update();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RateSnapshot> findLatest() {
        return jdbcClient
                .sql("select on_date, fetched_at, payload from rate_snapshot order by on_date desc limit 1")
                .query(this::toSnapshot)
                .optional();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RateSnapshot> findPrevious(LocalDate before) {
        return jdbcClient
                .sql(
                        """
                        select on_date, fetched_at, payload from rate_snapshot
                        where on_date < ? order by on_date desc limit 1
                        """)
                .param(before)
                .query(this::toSnapshot)
                .optional();
    }

    private RateSnapshot toSnapshot(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        var type = TypeFactory.createDefaultInstance().constructCollectionType(List.class, StoredRate.class);
        List<StoredRate> stored = objectMapper.readValue(rs.getString("payload"), type);

        var rates = new LinkedHashMap<String, CurrencyRate>(stored.size());
        for (var rate : stored) {
            rates.put(rate.code(), new CurrencyRate(rate.code(), rate.name(), rate.ratePerUnit()));
        }

        return new RateSnapshot(
                rs.getObject("on_date", LocalDate.class),
                rs.getObject("fetched_at", OffsetDateTime.class).toInstant(),
                rates);
    }

    public record StoredRate(String code, String name, BigDecimal ratePerUnit) {

        static List<StoredRate> from(RateSnapshot snapshot) {
            return snapshot.rates().values().stream()
                    .map(rate -> new StoredRate(rate.code(), rate.name(), rate.ratePerUnit()))
                    .toList();
        }
    }
}
