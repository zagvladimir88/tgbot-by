package ru.zagvladimir.tgbot.currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Conversion(BigDecimal amount, String from, String to, BigDecimal result, LocalDate onDate) {}
