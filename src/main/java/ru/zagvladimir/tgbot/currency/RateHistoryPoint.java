package ru.zagvladimir.tgbot.currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateHistoryPoint(LocalDate date, BigDecimal ratePerUnit) {}
