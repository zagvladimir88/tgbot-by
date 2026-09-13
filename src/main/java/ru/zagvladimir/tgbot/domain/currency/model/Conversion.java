package ru.zagvladimir.tgbot.domain.currency.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Conversion(BigDecimal amount, String from, String to, BigDecimal result, LocalDate onDate) {}
