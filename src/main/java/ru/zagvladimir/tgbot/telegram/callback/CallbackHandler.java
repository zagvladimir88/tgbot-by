package ru.zagvladimir.tgbot.telegram.callback;

import ru.zagvladimir.tgbot.telegram.BotRequest;

public interface CallbackHandler {

    public String prefix();

    public void handle(BotRequest.Callback callback);
}
