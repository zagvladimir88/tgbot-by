package ru.zagvladimir.tgbot.domain.weather;

import java.util.Optional;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;
import ru.zagvladimir.tgbot.domain.weather.model.Place;

public interface WeatherService {

    public Optional<Place> findPlace(String query);

    public Optional<Forecast> forecastFor(String city);

    public Forecast forecastAt(Place place);
}
