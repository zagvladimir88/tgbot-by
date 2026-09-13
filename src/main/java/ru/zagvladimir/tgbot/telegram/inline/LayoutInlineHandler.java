package ru.zagvladimir.tgbot.telegram.inline;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import ru.zagvladimir.tgbot.domain.layout.LayoutConverter;

@Component
@Order(100)
public class LayoutInlineHandler implements InlineHandler {

    private final LayoutConverter converter;

    LayoutInlineHandler(LayoutConverter converter) {
        this.converter = converter;
    }

    @Override
    public boolean supports(String query) {
        return !query.isBlank();
    }

    @Override
    public List<InlineQueryResult> results(String query) {
        var converted = converter.convert(query);
        if (converted.equals(query)) {
            return List.of();
        }

        return List.of(InlineQueryResultArticle.builder()
                .id("layout")
                .title(converted)
                .description("Исправленная раскладка")
                .inputMessageContent(
                        InputTextMessageContent.builder().messageText(converted).build())
                .build());
    }
}
