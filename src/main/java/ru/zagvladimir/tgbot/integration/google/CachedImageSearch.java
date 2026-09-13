package ru.zagvladimir.tgbot.integration.google;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.app.properties.ImageProperties;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchPage;
import ru.zagvladimir.tgbot.domain.image.port.ImageSearchPort;

@Component
public class CachedImageSearch implements ImageSearchPort {

    public static final String CACHE = "image-search";

    private final GoogleCseApi api;
    private final ImageProperties properties;
    private final CacheManager cacheManager;

    CachedImageSearch(GoogleCseApi api, ImageProperties properties, CacheManager cacheManager) {
        this.api = api;
        this.properties = properties;
        this.cacheManager = cacheManager;
    }

    @Override
    @Nullable
    public ImageSearchPage cachedPage(String query, int start) {
        var cache = cacheManager.getCache(CACHE);
        return cache == null ? null : cache.get(key(query, start), ImageSearchPage.class);
    }

    @Override
    @CachePut(cacheNames = CACHE, key = "#query + ':' + #start")
    public ImageSearchPage fetchPage(String query, int start) {
        var apiKey = Objects.requireNonNull(properties.apiKey(), "bot.image.api-key не задан");
        var cx = Objects.requireNonNull(properties.cx(), "bot.image.cx не задан");

        var response = api.search(apiKey, cx, query, "image", properties.pageSize(), start, "active");

        return new ImageSearchPage(
                query, start, GoogleCseMapper.toResults(response), GoogleCseMapper.hasMore(response));
    }

    private static String key(String query, int start) {
        return query + ":" + start;
    }
}
