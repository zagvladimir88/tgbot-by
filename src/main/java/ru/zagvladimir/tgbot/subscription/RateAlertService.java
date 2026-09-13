package ru.zagvladimir.tgbot.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.subscription.model.RateAlert;
import ru.zagvladimir.tgbot.subscription.model.RateAlertCondition;
import ru.zagvladimir.tgbot.subscription.port.RateAlertRepository;

@Service
public class RateAlertService {

    private final RateAlertRepository repository;

    RateAlertService(RateAlertRepository repository) {
        this.repository = repository;
    }

    public RateAlert create(
            long chatId,
            long userId,
            String currency,
            RateAlertCondition condition,
            BigDecimal threshold,
            boolean oneShot) {
        return repository.create(chatId, userId, currency, condition, threshold, oneShot);
    }

    public List<RateAlert> activeAlerts() {
        return repository.findActive();
    }

    public List<RateAlert> forChat(long chatId) {
        return repository.findByChat(chatId);
    }

    public boolean remove(long chatId, long alertId) {
        return repository.delete(chatId, alertId);
    }

    public void markFired(long alertId, LocalDate onDate) {
        repository.markFired(alertId, onDate);
    }
}
