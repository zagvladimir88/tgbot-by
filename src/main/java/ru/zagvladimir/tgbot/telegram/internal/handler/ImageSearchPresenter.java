package ru.zagvladimir.tgbot.telegram.internal.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.CRC32;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.zagvladimir.tgbot.image.ImageResult;
import ru.zagvladimir.tgbot.image.ImageSearchOutcome;
import ru.zagvladimir.tgbot.image.ImageSearchService;
import ru.zagvladimir.tgbot.telegram.internal.format.HtmlEscaper;

@Component
class ImageSearchPresenter {

    static final String CALLBACK_PREFIX = "img:";
    private static final int PAGE_SIZE = 10;

    private final ImageSearchService searchService;
    private final Cache<String, String> queriesById = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(6))
            .maximumSize(5_000)
            .build();

    ImageSearchPresenter(ImageSearchService searchService) {
        this.searchService = searchService;
    }

    String rememberQuery(String query) {
        var id = shortId(query);
        queriesById.put(id, query);
        return id;
    }

    @Nullable
    String queryById(String id) {
        return queriesById.getIfPresent(id);
    }

    ImageSearchOutcome searchAt(String query, int index) {
        var pageStart = ((index - 1) / PAGE_SIZE) * PAGE_SIZE + 1;
        return searchService.search(query, pageStart);
    }

    @Nullable
    ImageResult pick(ImageSearchOutcome.Found found, int index) {
        var offset = (index - 1) % PAGE_SIZE;
        var results = found.page().results();
        return offset < results.size() ? results.get(offset) : null;
    }

    String caption(String query, ImageResult result, int index) {
        return "%d. <b>%s</b>%s%s"
                .formatted(
                        index,
                        HtmlEscaper.escape(shorten(result.title())),
                        System.lineSeparator(),
                        HtmlEscaper.escape(query));
    }

    InlineKeyboardMarkup keyboard(String queryId, int index, boolean hasMore) {
        var buttons = new ArrayList<InlineKeyboardButton>(3);

        if (index > 1) {
            buttons.add(button("◀", queryId, index - 1));
        }
        buttons.add(button("🔀", queryId, randomNeighbour(index)));
        if (hasMore || index % PAGE_SIZE != 0) {
            buttons.add(button("▶", queryId, index + 1));
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(buttons))
                .build();
    }

    static String describe(ImageSearchOutcome outcome) {
        return switch (outcome) {
            case ImageSearchOutcome.NothingFound nothing -> "Ничего не нашёл по запросу: " + nothing.query();
            case ImageSearchOutcome.QuotaExceeded quota ->
                "На сегодня исчерпан лимит поиска картинок (%d запросов). Попробуйте завтра."
                        .formatted(quota.dailyLimit());
            case ImageSearchOutcome.Unavailable unavailable ->
                unavailable.reason() == null
                        ? "Поиск картинок временно недоступен."
                        : "Поиск картинок недоступен: " + unavailable.reason();
            case ImageSearchOutcome.Found found -> "";
        };
    }

    static List<String> parseCallback(String data) {
        return List.of(data.substring(CALLBACK_PREFIX.length()).split(":"));
    }

    private static InlineKeyboardButton button(String text, String queryId, int index) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(CALLBACK_PREFIX + queryId + ":" + index)
                .build();
    }

    private static int randomNeighbour(int index) {
        var page = (index - 1) / PAGE_SIZE;
        return page * PAGE_SIZE + 1 + (int) (Math.random() * PAGE_SIZE);
    }

    private static String shorten(String title) {
        return title.length() <= 80 ? title : title.substring(0, 77) + "...";
    }

    private static String shortId(String query) {
        var crc = new CRC32();
        crc.update(query.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().toHexDigits((int) crc.getValue());
    }
}
