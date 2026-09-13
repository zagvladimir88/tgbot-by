package ru.zagvladimir.tgbot.image.internal;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.image.ImageResult;
import ru.zagvladimir.tgbot.image.ImageSearchOutcome;
import ru.zagvladimir.tgbot.image.ImageSearchPage;
import ru.zagvladimir.tgbot.image.ImageSearchService;

@Service
class GoogleImageSearchService implements ImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(GoogleImageSearchService.class);

    private final CachedImageSearch search;
    private final CseQuotaCounter quota;
    private final ImageProperties properties;

    GoogleImageSearchService(CachedImageSearch search, CseQuotaCounter quota, ImageProperties properties) {
        this.search = search;
        this.quota = quota;
        this.properties = properties;
    }

    @Override
    public ImageSearchOutcome search(String query, int start) {
        if (!properties.configured()) {
            return new ImageSearchOutcome.Unavailable("не настроены bot.image.api-key и bot.image.cx");
        }

        var normalized = query.trim();
        if (normalized.isEmpty()) {
            return new ImageSearchOutcome.NothingFound(query);
        }

        var cached = search.cachedPage(normalized, start);
        if (cached != null) {
            return toOutcome(cached, normalized);
        }

        if (!quota.tryConsume(properties.dailyLimit())) {
            return new ImageSearchOutcome.QuotaExceeded(properties.dailyLimit());
        }

        try {
            return toOutcome(search.fetchPage(normalized, start), normalized);
        } catch (Exception e) {
            log.warn("Поиск картинок по запросу \"{}\" не удался: {}", normalized, e.toString());
            return new ImageSearchOutcome.Unavailable(null);
        }
    }

    private static ImageSearchOutcome toOutcome(ImageSearchPage page, String query) {
        return page.isEmpty() ? new ImageSearchOutcome.NothingFound(query) : new ImageSearchOutcome.Found(page);
    }

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
