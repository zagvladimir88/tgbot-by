package ru.zagvladimir.tgbot.weather.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import ru.zagvladimir.tgbot.weather.Place;
import ru.zagvladimir.tgbot.weather.WeatherCondition;

class OpenMeteoClientTest {

    private static final String GEOCODING_BODY =
            """
            {
              "results": [
                {
                  "id": 625144,
                  "name": "Минск",
                  "latitude": 53.90019,
                  "longitude": 27.56653,
                  "country": "Беларусь",
                  "country_code": "BY",
                  "admin1": "Минск",
                  "timezone": "Europe/Minsk",
                  "population": 1742124
                }
              ]
            }
            """;

    private static final String FORECAST_BODY =
            """
            {
              "timezone": "Europe/Minsk",
              "utc_offset_seconds": 10800,
              "current": {
                "time": "2026-09-13T19:00",
                "temperature_2m": 17.9,
                "apparent_temperature": 16.0,
                "relative_humidity_2m": 50,
                "weather_code": 3,
                "wind_speed_10m": 8.6
              },
              "daily": {
                "time": ["2026-09-13", "2026-09-14"],
                "weather_code": [3, 51],
                "temperature_2m_max": [18.7, 16.6],
                "temperature_2m_min": [10.8, 12.8]
              }
            }
            """;

    private WireMockServer server;

    @BeforeEach
    void startServer() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void mapsGeocodingResponseToPlace() {
        server.stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(GEOCODING_BODY)));

        var place = geocoding().findPlace("Минск");

        assertThat(place).get().satisfies(found -> {
            assertThat(found.name()).isEqualTo("Минск");
            assertThat(found.country()).isEqualTo("Беларусь");
            assertThat(found.latitude()).isEqualTo(53.90019);
            assertThat(found.zoneId()).isEqualTo(ZoneId.of("Europe/Minsk"));
            assertThat(found.fullName()).isEqualTo("Минск, Беларусь");
        });
    }

    @Test
    void returnsEmptyWhenCityIsUnknown() {
        server.stubFor(get(urlPathEqualTo("/v1/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"generationtime_ms\": 0.1}")));

        assertThat(geocoding().findPlace("Ниоткуда")).isEmpty();
    }

    @Test
    void mapsForecastResponseIncludingWmoCodes() {
        server.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(FORECAST_BODY)));

        var forecast = forecast().forecastAt(minsk());

        assertThat(forecast.current().temperature()).isEqualTo(17.9);
        assertThat(forecast.current().humidity()).isEqualTo(50);
        assertThat(forecast.current().condition()).isEqualTo(WeatherCondition.OVERCAST);
        assertThat(forecast.daily()).hasSize(2);
        assertThat(forecast.daily().getFirst().date()).isEqualTo(LocalDate.of(2026, 9, 13));
        assertThat(forecast.daily().getFirst().maxTemperature()).isEqualTo(18.7);
        assertThat(forecast.daily().get(1).condition()).isEqualTo(WeatherCondition.DRIZZLE);
    }

    private GeocodingLookup geocoding() {
        return new GeocodingLookup(client(OpenMeteoGeocodingApi.class));
    }

    private ForecastLookup forecast() {
        return new ForecastLookup(client(OpenMeteoForecastApi.class));
    }

    private <T> T client(Class<T> type) {
        var properties = new WeatherProperties(server.baseUrl(), server.baseUrl(), null);
        return WeatherClientConfiguration.createClient(RestClient.builder(), server.baseUrl(), properties, type);
    }

    private static Place minsk() {
        return new Place("Минск", "Беларусь", "Минск", 53.90019, 27.56653, ZoneId.of("Europe/Minsk"));
    }
}
