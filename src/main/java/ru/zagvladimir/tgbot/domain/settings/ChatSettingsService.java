package ru.zagvladimir.tgbot.domain.settings;

import java.util.List;
import ru.zagvladimir.tgbot.domain.settings.model.ChatSettings;

public interface ChatSettingsService {

    public ChatSettings find(long chatId);

    public void setDefaultCity(long chatId, String city);

    public void setDefaultCurrencies(long chatId, List<String> currencies);
}
