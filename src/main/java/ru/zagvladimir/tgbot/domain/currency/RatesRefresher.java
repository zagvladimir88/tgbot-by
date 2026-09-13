package ru.zagvladimir.tgbot.domain.currency;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.currency.event.RatesUpdated;
import ru.zagvladimir.tgbot.domain.currency.model.RateSnapshot;
import ru.zagvladimir.tgbot.domain.currency.port.RateSnapshotRepository;
import ru.zagvladimir.tgbot.domain.currency.port.RatesPort;

@Component
public class RatesRefresher {

    private static final Logger log = LoggerFactory.getLogger(RatesRefresher.class);

    private final RatesPort ratesPort;
    private final RateSnapshotRepository repository;
    private final ApplicationEventPublisher events;

    RatesRefresher(RatesPort ratesPort, RateSnapshotRepository repository, ApplicationEventPublisher events) {
        this.ratesPort = ratesPort;
        this.repository = repository;
        this.events = events;
    }

    @Scheduled(initialDelayString = "PT10S", fixedDelayString = "PT1H")
    void refreshPeriodically() {
        refresh();
    }

    public Optional<RateSnapshot> refresh() {
        RateSnapshot fetched;
        try {
            fetched = ratesPort.fetchDaily();
        } catch (Exception e) {
            log.warn("Не удалось получить курсы НБРБ, остаёмся на последнем снимке: {}", e.toString());
            return repository.findLatest();
        }

        var known = repository.findLatest();
        var isNewDate = known.isEmpty() || fetched.onDate().isAfter(known.get().onDate());

        repository.save(fetched);

        if (isNewDate) {
            log.info("Получены курсы НБРБ на {}", fetched.onDate());
            events.publishEvent(new RatesUpdated(fetched.onDate()));
        }

        return Optional.of(fetched);
    }
}
