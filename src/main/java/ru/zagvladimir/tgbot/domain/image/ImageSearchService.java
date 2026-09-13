package ru.zagvladimir.tgbot.domain.image;

import ru.zagvladimir.tgbot.domain.image.model.ImageSearchOutcome;

public interface ImageSearchService {

    public ImageSearchOutcome search(String query, int start);
}
