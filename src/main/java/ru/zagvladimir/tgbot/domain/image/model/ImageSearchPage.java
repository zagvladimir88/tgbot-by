package ru.zagvladimir.tgbot.domain.image.model;

import java.util.List;

public record ImageSearchPage(String query, int start, List<ImageResult> results, boolean hasMore) {

    public ImageSearchPage {
        results = List.copyOf(results);
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }
}
