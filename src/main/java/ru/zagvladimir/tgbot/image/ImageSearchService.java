package ru.zagvladimir.tgbot.image;

public interface ImageSearchService {

    ImageSearchOutcome search(String query, int start);
}
