package ru.zagvladimir.tgbot.subscription;

import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.subscription.event.SubscriptionDue;
import ru.zagvladimir.tgbot.subscription.port.SubscriptionRepository;

@Component
public class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);
    private static final int BATCH_SIZE = 50;

    private final SubscriptionRepository repository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    SubscriptionScheduler(SubscriptionRepository repository, ApplicationEventPublisher events, Clock clock) {
        this.repository = repository;
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
    public int fireDueSubscriptions() {
        var now = Instant.now(clock);
        var due = repository.lockDue(now, BATCH_SIZE);

        for (var subscription : due) {
            var nextRun = SubscriptionService.nextRunAfter(subscription.cron(), subscription.zoneId(), now);
            repository.reschedule(subscription.id(), nextRun, now);

            events.publishEvent(new SubscriptionDue(
                    subscription.id(), subscription.chatId(), subscription.type(), subscription.payload()));
        }

        return due.size();
    }
}
