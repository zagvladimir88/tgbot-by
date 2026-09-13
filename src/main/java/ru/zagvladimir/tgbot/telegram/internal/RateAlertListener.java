package ru.zagvladimir.tgbot.telegram.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.currency.CurrencyService;
import ru.zagvladimir.tgbot.currency.RateSnapshot;
import ru.zagvladimir.tgbot.currency.RateWithDelta;
import ru.zagvladimir.tgbot.currency.RatesUpdated;
import ru.zagvladimir.tgbot.subscription.RateAlert;
import ru.zagvladimir.tgbot.subscription.RateAlertService;
import ru.zagvladimir.tgbot.telegram.MessageSender;

@Component
class RateAlertListener {

    private static final Logger log = LoggerFactory.getLogger(RateAlertListener.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final RateAlertService alerts;
    private final CurrencyService currencyService;
    private final MessageSender sender;

    RateAlertListener(RateAlertService alerts, CurrencyService currencyService, MessageSender sender) {
        this.alerts = alerts;
        this.currencyService = currencyService;
        this.sender = sender;
    }

    @ApplicationModuleListener
    void onRatesUpdated(RatesUpdated event) {
        var snapshot = currencyService.latestRates().orElse(null);
        if (snapshot == null) {
            return;
        }

        for (var alert : alerts.activeAlerts()) {
            if (event.onDate().equals(alert.lastFiredOnDate())) {
                continue;
            }

            var withDelta = currencyService.ratesWithDelta(java.util.List.of(alert.currency()));
            if (withDelta.isEmpty()) {
                continue;
            }

            if (triggered(alert, withDelta.getFirst())) {
                fire(alert, withDelta.getFirst(), snapshot);
            }
        }
    }

    private void fire(RateAlert alert, RateWithDelta current, RateSnapshot snapshot) {
        log.info("Алерт {} по {} сработал", alert.id(), alert.currency());

        sender.sendHtml(
                alert.chatId(),
                "🔔 <b>%s</b> — %s BYN%sУсловие: %s"
                        .formatted(
                                alert.currency(),
                                current.rate().ratePerUnit().setScale(4, RoundingMode.HALF_UP),
                                System.lineSeparator(),
                                describe(alert)));

        alerts.markFired(alert.id(), snapshot.onDate());
    }

    static boolean triggered(RateAlert alert, RateWithDelta current) {
        var rate = current.rate().ratePerUnit();

        return switch (alert.condition()) {
            case ABOVE -> rate.compareTo(alert.threshold()) > 0;
            case BELOW -> rate.compareTo(alert.threshold()) < 0;
            case CHANGE_PERCENT -> changedEnough(alert, current);
        };
    }

    private static boolean changedEnough(RateAlert alert, RateWithDelta current) {
        var delta = current.delta();
        if (delta == null || delta.signum() == 0) {
            return false;
        }

        var previous = current.rate().ratePerUnit().subtract(delta);
        if (previous.signum() == 0) {
            return false;
        }

        var percent = delta.abs().multiply(HUNDRED).divide(previous.abs(), 4, RoundingMode.HALF_UP);
        return percent.compareTo(alert.threshold()) >= 0;
    }

    private static String describe(RateAlert alert) {
        return switch (alert.condition()) {
            case ABOVE -> "выше " + alert.threshold();
            case BELOW -> "ниже " + alert.threshold();
            case CHANGE_PERCENT -> "изменение больше " + alert.threshold() + "%";
        };
    }
}
