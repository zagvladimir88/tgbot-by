package ru.zagvladimir.tgbot.domain.weather.port;

import java.util.Optional;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;
import ru.zagvladimir.tgbot.domain.weather.model.Place;

public interface WeatherPort {

    Optional<Place> findPlace(String query);

    Forecast forecastAt(Place place);
}
