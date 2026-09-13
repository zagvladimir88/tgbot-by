package ru.zagvladimir.tgbot.domain.image.model;

import org.jspecify.annotations.Nullable;

public record ImageResult(
        String title,
        String imageUrl,
        @Nullable String thumbnailUrl,
        @Nullable String contextUrl,
        int width,
        int height) {}
