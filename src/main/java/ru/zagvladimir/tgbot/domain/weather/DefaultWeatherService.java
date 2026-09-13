package ru.zagvladimir.tgbot.domain.weather;

import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;
import ru.zagvladimir.tgbot.domain.weather.model.Place;
import ru.zagvladimir.tgbot.domain.weather.port.WeatherPort;

@Service
public class DefaultWeatherService implements WeatherService {

    private final WeatherPort weatherPort;

    DefaultWeatherService(WeatherPort weatherPort) {
        this.weatherPort = weatherPort;
    }

    @Override
    public Optional<Place> findPlace(String query) {
        return weatherPort.findPlace(query);
    }

    @Override
    public Optional<Forecast> forecastFor(String city) {
        return weatherPort.findPlace(city).map(weatherPort::forecastAt);
    }

    @Override
    public Forecast forecastAt(Place place) {
        return weatherPort.forecastAt(place);
    }
}
