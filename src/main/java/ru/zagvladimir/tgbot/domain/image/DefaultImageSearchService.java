package ru.zagvladimir.tgbot.domain.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.app.properties.ImageProperties;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchOutcome;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchPage;
import ru.zagvladimir.tgbot.domain.image.port.ImageSearchPort;
import ru.zagvladimir.tgbot.domain.image.port.SearchQuotaPort;

@Service
public class DefaultImageSearchService implements ImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(DefaultImageSearchService.class);

    private final ImageSearchPort searchPort;
    private final SearchQuotaPort quotaPort;
    private final ImageProperties properties;

    DefaultImageSearchService(ImageSearchPort searchPort, SearchQuotaPort quotaPort, ImageProperties properties) {
        this.searchPort = searchPort;
        this.quotaPort = quotaPort;
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

        var cached = searchPort.cachedPage(normalized, start);
        if (cached != null) {
            return toOutcome(cached, normalized);
        }

        if (!quotaPort.tryConsume(properties.dailyLimit())) {
            return new ImageSearchOutcome.QuotaExceeded(properties.dailyLimit());
        }

        try {
            return toOutcome(searchPort.fetchPage(normalized, start), normalized);
        } catch (Exception e) {
            log.warn("Поиск картинок по запросу \"{}\" не удался: {}", normalized, e.toString());
            return new ImageSearchOutcome.Unavailable(null);
        }
    }

    private static ImageSearchOutcome toOutcome(ImageSearchPage page, String query) {
        return page.isEmpty() ? new ImageSearchOutcome.NothingFound(query) : new ImageSearchOutcome.Found(page);
    }
}
