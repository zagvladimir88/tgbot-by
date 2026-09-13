package ru.zagvladimir.tgbot.telegram.inline;

import java.util.List;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;

public interface InlineHandler {

    public boolean supports(String query);

    public List<InlineQueryResult> results(String query);
}
