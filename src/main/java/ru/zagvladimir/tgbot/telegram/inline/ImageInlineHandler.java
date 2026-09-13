package ru.zagvladimir.tgbot.telegram.inline;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchOutcome;
import ru.zagvladimir.tgbot.telegram.presenter.ImageSearchPresenter;

@Component
@Order(50)
public class ImageInlineHandler implements InlineHandler {

    private static final String TRIGGER = "img ";

    private final ImageSearchPresenter presenter;

    ImageInlineHandler(ImageSearchPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean supports(String query) {
        return query.toLowerCase(java.util.Locale.ROOT).startsWith(TRIGGER);
    }

    @Override
    public List<InlineQueryResult> results(String query) {
        var search = query.substring(TRIGGER.length()).trim();
        if (search.isEmpty()) {
            return List.of();
        }

        if (!(presenter.searchAt(search, 1) instanceof ImageSearchOutcome.Found found)) {
            return List.of();
        }

        return found.page().results().stream()
                .filter(result -> result.thumbnailUrl() != null)
                .limit(20)
                .map(result -> (InlineQueryResult) InlineQueryResultPhoto.builder()
                        .id(String.valueOf(result.imageUrl().hashCode()))
                        .photoUrl(result.imageUrl())
                        .thumbnailUrl(result.thumbnailUrl())
                        .photoWidth(result.width())
                        .photoHeight(result.height())
                        .title(result.title())
                        .build())
                .toList();
    }
}
