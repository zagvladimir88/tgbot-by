package ru.zagvladimir.tgbot.domain.image.port;

public interface SearchQuotaPort {

    boolean tryConsume(int dailyLimit);

    int usedToday();
}
