package ru.zagvladimir.tgbot.subscription;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.subscription.model.Subscription;
import ru.zagvladimir.tgbot.subscription.model.SubscriptionType;
import ru.zagvladimir.tgbot.subscription.port.SubscriptionRepository;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final Clock clock;

    SubscriptionService(SubscriptionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Subscription subscribe(
            long chatId, long userId, SubscriptionType type, String payload, String cron, ZoneId zoneId) {
        var nextRun = nextRunAfter(cron, zoneId, Instant.now(clock));
        return repository.create(chatId, userId, type, payload, cron, zoneId, nextRun);
    }

    public List<Subscription> forChat(long chatId) {
        return repository.findByChat(chatId);
    }

    public boolean unsubscribe(long chatId, long subscriptionId) {
        return repository.delete(chatId, subscriptionId);
    }

    public static Instant nextRunAfter(String cron, ZoneId zoneId, Instant from) {
        var next = CronExpression.parse(cron).next(from.atZone(zoneId));
        if (next == null) {
            throw new IllegalArgumentException("Расписание %s больше никогда не сработает".formatted(cron));
        }
        return next.toInstant();
    }
}
