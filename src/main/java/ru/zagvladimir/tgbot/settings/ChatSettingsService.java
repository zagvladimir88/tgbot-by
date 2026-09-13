package ru.zagvladimir.tgbot.settings;

import java.util.List;

public interface ChatSettingsService {

    ChatSettings find(long chatId);

    void setDefaultCity(long chatId, String city);

    void setDefaultCurrencies(long chatId, List<String> currencies);
}
