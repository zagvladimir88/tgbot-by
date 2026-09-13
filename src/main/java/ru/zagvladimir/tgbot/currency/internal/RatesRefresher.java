package ru.zagvladimir.tgbot.currency.internal;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.currency.RateSnapshot;
import ru.zagvladimir.tgbot.currency.RatesUpdated;

@Component
class RatesRefresher {

    private static final Logger log = LoggerFactory.getLogger(RatesRefresher.class);

    private final NbrbRatesFetcher fetcher;
    private final RateSnapshotStore store;
    private final ApplicationEventPublisher events;

    RatesRefresher(NbrbRatesFetcher fetcher, RateSnapshotStore store, ApplicationEventPublisher events) {
        this.fetcher = fetcher;
        this.store = store;
        this.events = events;
    }

    @Scheduled(initialDelayString = "PT10S", fixedDelayString = "PT1H")
    void refreshPeriodically() {
        refresh();
    }

    Optional<RateSnapshot> refresh() {
        RateSnapshot fetched;
        try {
            fetched = fetcher.fetchDaily();
        } catch (Exception e) {
            log.warn("Не удалось получить курсы НБРБ, остаёмся на последнем снимке: {}", e.toString());
            return store.findLatest();
        }

        var known = store.findLatest();
        var isNewDate = known.isEmpty() || fetched.onDate().isAfter(known.get().onDate());

        store.save(fetched);

        if (isNewDate) {
            log.info("Получены курсы НБРБ на {}", fetched.onDate());
            events.publishEvent(new RatesUpdated(fetched.onDate()));
        }

        return Optional.of(fetched);
    }
}
