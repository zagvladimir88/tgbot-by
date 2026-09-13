package ru.zagvladimir.tgbot.domain.currency.port;

import java.time.LocalDate;
import java.util.Optional;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;

public interface RateSnapshotRepository {

    void save(RateSnapshot snapshot);

    Optional<RateSnapshot> findLatest();

    Optional<RateSnapshot> findPrevious(LocalDate before);
}
