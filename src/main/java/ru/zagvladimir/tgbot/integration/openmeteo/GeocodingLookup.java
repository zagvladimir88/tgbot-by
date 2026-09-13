package ru.zagvladimir.tgbot.integration.openmeteo;

import java.time.ZoneId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.domain.settings.model.ChatSettings;
import ru.zagvladimir.tgbot.domain.weather.model.Place;

@Service
public class GeocodingLookup {

    private static final Logger log = LoggerFactory.getLogger(GeocodingLookup.class);

    private final OpenMeteoGeocodingApi api;

    GeocodingLookup(OpenMeteoGeocodingApi api) {
        this.api = api;
    }

    @Cacheable(cacheNames = "geocoding", unless = "#result == null")
    public Optional<Place> findPlace(String query) {
        var response = api.search(query, 1, "ru", "json");
        var results = response.results();
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }

        var first = results.getFirst();
        log.debug("Город {} определён как {} ({}, {})", query, first.name(), first.latitude(), first.longitude());

        return Optional.of(new Place(
                first.name(),
                first.country(),
                first.admin1(),
                first.latitude(),
                first.longitude(),
                zoneOf(first.timezone())));
    }

    private static ZoneId zoneOf(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ChatSettings.DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException e) {
            return ChatSettings.DEFAULT_ZONE;
        }
    }
}
