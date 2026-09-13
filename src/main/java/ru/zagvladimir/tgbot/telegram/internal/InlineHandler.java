package ru.zagvladimir.tgbot.telegram.internal;

import java.util.List;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;

public interface InlineHandler {

    boolean supports(String query);

    List<InlineQueryResult> results(String query);
}
