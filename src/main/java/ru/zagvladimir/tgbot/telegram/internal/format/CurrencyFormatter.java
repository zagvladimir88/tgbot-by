package ru.zagvladimir.tgbot.telegram.internal.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.currency.Conversion;
import ru.zagvladimir.tgbot.currency.RateWithDelta;

@Component
public class CurrencyFormatter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int RATE_SCALE = 4;
    private static final int MONEY_SCALE = 2;

    public String formatRates(List<RateWithDelta> rates, LocalDate onDate) {
        if (rates.isEmpty()) {
            return "Курсы пока недоступны, попробуйте позже.";
        }

        var text = new StringBuilder();
        text.append("💱 <b>Курсы НБРБ на ")
                .append(DATE.format(onDate))
                .append("</b>")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        for (var entry : rates) {
            text.append(entry.rate().code())
                    .append("  ")
                    .append(rate(entry.rate().ratePerUnit()))
                    .append(deltaSuffix(entry))
                    .append(System.lineSeparator());
        }

        return text.toString().strip();
    }

    public String formatConversion(Conversion conversion) {
        return "%s %s = <b>%s %s</b>%sКурс НБРБ на %s"
                .formatted(
                        money(conversion.amount()),
                        conversion.from(),
                        money(conversion.result()),
                        conversion.to(),
                        System.lineSeparator(),
                        DATE.format(conversion.onDate()));
    }

    private String deltaSuffix(RateWithDelta entry) {
        var delta = entry.delta();
        if (delta == null || delta.signum() == 0) {
            return "";
        }

        var arrow = entry.grew() ? " ↑ +" : " ↓ ";
        return arrow + rate(delta);
    }

    private static String rate(BigDecimal value) {
        return value.setScale(RATE_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

    private static String money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
