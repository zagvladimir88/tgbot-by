package ru.zagvladimir.tgbot.integration.openmeteo;

import java.util.Optional;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.weather.model.Forecast;
import ru.zagvladimir.tgbot.domain.weather.model.Place;
import ru.zagvladimir.tgbot.domain.weather.port.WeatherPort;

@Component
public class OpenMeteoWeatherAdapter implements WeatherPort {

    private final GeocodingLookup geocoding;
    private final ForecastLookup forecast;

    OpenMeteoWeatherAdapter(GeocodingLookup geocoding, ForecastLookup forecast) {
        this.geocoding = geocoding;
        this.forecast = forecast;
    }

    @Override
    public Optional<Place> findPlace(String query) {
        return geocoding.findPlace(query);
    }

    @Override
    public Forecast forecastAt(Place place) {
        return forecast.forecastAt(place);
    }
}
