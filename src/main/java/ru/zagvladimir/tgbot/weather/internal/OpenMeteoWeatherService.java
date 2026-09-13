package ru.zagvladimir.tgbot.weather.internal;

import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.zagvladimir.tgbot.weather.Forecast;
import ru.zagvladimir.tgbot.weather.Place;
import ru.zagvladimir.tgbot.weather.WeatherService;

@Service
class OpenMeteoWeatherService implements WeatherService {

    private final GeocodingLookup geocoding;
    private final ForecastLookup forecast;

    OpenMeteoWeatherService(GeocodingLookup geocoding, ForecastLookup forecast) {
        this.geocoding = geocoding;
        this.forecast = forecast;
    }

    @Override
    public Optional<Place> findPlace(String query) {
        return geocoding.findPlace(query);
    }

    @Override
    public Optional<Forecast> forecastFor(String city) {
        return geocoding.findPlace(city).map(forecast::forecastAt);
    }

    @Override
    public Forecast forecastAt(Place place) {
        return forecast.forecastAt(place);
    }
}
