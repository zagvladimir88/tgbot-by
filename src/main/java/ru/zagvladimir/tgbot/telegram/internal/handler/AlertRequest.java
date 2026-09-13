package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import ru.zagvladimir.tgbot.subscription.RateAlertCondition;

record AlertRequest(String currency, RateAlertCondition condition, BigDecimal threshold) {

    private static final Pattern THRESHOLD =
            Pattern.compile("^\\s*([a-zA-Zа-яА-Я]{3})\\s*(>|<|больше|меньше)\\s*(\\d+(?:[.,]\\d+)?)\\s*$");

    private static final Pattern CHANGE =
            Pattern.compile("^\\s*([a-zA-Zа-яА-Я]{3})\\s+(?:change|изменение)\\s*>?\\s*(\\d+(?:[.,]\\d+)?)\\s*%?\\s*$");

    static Optional<AlertRequest> parse(String input) {
        var change = CHANGE.matcher(input);
        if (change.matches()) {
            return Optional.of(new AlertRequest(
                    code(change.group(1)), RateAlertCondition.CHANGE_PERCENT, number(change.group(2))));
        }

        var threshold = THRESHOLD.matcher(input);
        if (threshold.matches()) {
            var condition =
                    switch (threshold.group(2)) {
                        case ">", "больше" -> RateAlertCondition.ABOVE;
                        default -> RateAlertCondition.BELOW;
                    };
            return Optional.of(new AlertRequest(code(threshold.group(1)), condition, number(threshold.group(3))));
        }

        return Optional.empty();
    }

    private static String code(String raw) {
        return raw.toUpperCase(Locale.ROOT);
    }

    private static BigDecimal number(String raw) {
        return new BigDecimal(raw.replace(',', '.'));
    }
}
