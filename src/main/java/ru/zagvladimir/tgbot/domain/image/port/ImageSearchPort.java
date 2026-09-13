package ru.zagvladimir.tgbot.domain.image.port;

import org.jspecify.annotations.Nullable;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchPage;

public interface ImageSearchPort {

    @Nullable
    ImageSearchPage cachedPage(String query, int start);

    ImageSearchPage fetchPage(String query, int start);
}
