package ru.zagvladimir.tgbot.domain.currency.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateHistoryPoint(LocalDate date, BigDecimal ratePerUnit) {}
