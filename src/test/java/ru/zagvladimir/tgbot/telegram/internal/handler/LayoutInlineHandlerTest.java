package ru.zagvladimir.tgbot.telegram.internal.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import ru.zagvladimir.tgbot.layout.LayoutConverter;

class LayoutInlineHandlerTest {

    private final LayoutInlineHandler handler = new LayoutInlineHandler(new LayoutConverter());

    @Test
    void ignoresBlankQuery() {
        assertThat(handler.supports("")).isFalse();
        assertThat(handler.supports("   ")).isFalse();
        assertThat(handler.supports("ghbdtn")).isTrue();
    }

    @Test
    void returnsConvertedTextAsArticle() {
        var results = handler.results("ghbdtn");

        assertThat(results).singleElement().isInstanceOfSatisfying(InlineQueryResultArticle.class, article -> {
            assertThat(article.getTitle()).isEqualTo("привет");
            assertThat(article.getId()).isEqualTo("layout");
        });
    }

    @Test
    void returnsNothingWhenConversionChangesNothing() {
        assertThat(handler.results("12345")).isEmpty();
    }
}
