package ru.zagvladimir.tgbot.weather;

import java.util.Optional;

public interface WeatherService {

    Optional<Place> findPlace(String query);

    Optional<Forecast> forecastFor(String city);

    Forecast forecastAt(Place place);
}
