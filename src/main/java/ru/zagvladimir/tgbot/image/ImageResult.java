package ru.zagvladimir.tgbot.image;

import org.jspecify.annotations.Nullable;

public record ImageResult(
        String title,
        String imageUrl,
        @Nullable String thumbnailUrl,
        @Nullable String contextUrl,
        int width,
        int height) {}
