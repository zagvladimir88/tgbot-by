package ru.zagvladimir.tgbot.telegram.internal.handler;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

record ConversionRequest(BigDecimal amount, String from, String to) {

    private static final Pattern PATTERN = Pattern.compile(
            "^\\s*(\\d+(?:[.,]\\d+)?)\\s*([a-zA-Zа-яА-Я]{3})\\s*(?:(?:в|to|->)\\s*)?([a-zA-Zа-яА-Я]{3})?\\s*$");

    static Optional<ConversionRequest> parse(String input, String defaultTarget) {
        var matcher = PATTERN.matcher(input);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        var amount = new BigDecimal(matcher.group(1).replace(',', '.'));
        var from = matcher.group(2).toUpperCase(Locale.ROOT);
        var to = matcher.group(3) == null ? defaultTarget : matcher.group(3).toUpperCase(Locale.ROOT);

        return Optional.of(new ConversionRequest(amount, from, to));
    }
}
