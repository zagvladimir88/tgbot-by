package ru.zagvladimir.tgbot.telegram.internal;

public interface CallbackHandler {

    String prefix();

    void handle(BotRequest.Callback callback);
}
