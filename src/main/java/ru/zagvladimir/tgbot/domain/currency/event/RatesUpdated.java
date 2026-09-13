package ru.zagvladimir.tgbot.domain.currency.event;

import java.time.LocalDate;

public record RatesUpdated(LocalDate onDate) {}
