package ru.zagvladimir.tgbot.image.internal;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.image.ImageSearchPage;

@Component
class CachedImageSearch {

    static final String CACHE = "image-search";

    private final GoogleCseApi api;
    private final ImageProperties properties;
    private final CacheManager cacheManager;

    CachedImageSearch(GoogleCseApi api, ImageProperties properties, CacheManager cacheManager) {
        this.api = api;
        this.properties = properties;
        this.cacheManager = cacheManager;
    }

    @Nullable
    ImageSearchPage cachedPage(String query, int start) {
        var cache = cacheManager.getCache(CACHE);
        return cache == null ? null : cache.get(key(query, start), ImageSearchPage.class);
    }

    @CachePut(cacheNames = CACHE, key = "#query + ':' + #start")
    ImageSearchPage fetchPage(String query, int start) {
        var response = api.search(
                properties.apiKey(), properties.cx(), query, "image", properties.pageSize(), start, "active");

        return new ImageSearchPage(
                query, start, GoogleImageSearchService.toResults(response), GoogleImageSearchService.hasMore(response));
    }

    private static String key(String query, int start) {
        return query + ":" + start;
    }
}
