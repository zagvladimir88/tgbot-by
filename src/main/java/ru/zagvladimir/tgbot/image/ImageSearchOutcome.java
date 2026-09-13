package ru.zagvladimir.tgbot.image;

import org.jspecify.annotations.Nullable;

public sealed interface ImageSearchOutcome {

    record Found(ImageSearchPage page) implements ImageSearchOutcome {}

    record NothingFound(String query) implements ImageSearchOutcome {}

    record QuotaExceeded(int dailyLimit) implements ImageSearchOutcome {}

    record Unavailable(@Nullable String reason) implements ImageSearchOutcome {}
}
