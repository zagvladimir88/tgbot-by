package ru.zagvladimir.tgbot.integration.google;

import java.util.List;
import java.util.Objects;
import ru.zagvladimir.tgbot.domain.image.model.ImageResult;
import ru.zagvladimir.tgbot.integration.google.dto.GoogleCseResponse;

final class GoogleCseMapper {

    private GoogleCseMapper() {}

    static List<ImageResult> toResults(GoogleCseResponse response) {
        var items = response.items();
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item.link() != null)
                .map(item -> new ImageResult(
                        Objects.requireNonNullElse(item.title(), "Без названия"),
                        item.link(),
                        item.image() == null ? null : item.image().thumbnailLink(),
                        item.image() == null ? null : item.image().contextLink(),
                        item.image() == null || item.image().width() == null
                                ? 0
                                : item.image().width(),
                        item.image() == null || item.image().height() == null
                                ? 0
                                : item.image().height()))
                .toList();
    }

    static boolean hasMore(GoogleCseResponse response) {
        var queries = response.queries();
        return queries != null
                && queries.nextPage() != null
                && !queries.nextPage().isEmpty();
    }
}
