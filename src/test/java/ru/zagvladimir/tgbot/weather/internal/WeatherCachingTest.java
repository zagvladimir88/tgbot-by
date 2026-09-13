package ru.zagvladimir.tgbot.weather.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;
import ru.zagvladimir.tgbot.weather.WeatherService;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WeatherCachingTest {

    private static final WireMockServer SERVER =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        SERVER.start();
    }

    @DynamicPropertySource
    static void openMeteoUrls(DynamicPropertyRegistry registry) {
        registry.add("bot.weather.geocoding-url", SERVER::baseUrl);
        registry.add("bot.weather.forecast-url", SERVER::baseUrl);
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop();
    }

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void resetState() {
        SERVER.resetRequests();
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        SERVER.stubFor(
                get(urlPathEqualTo("/v1/search"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {"results":[{"name":"Минск","latitude":53.9,"longitude":27.57,
                                "country":"Беларусь","country_code":"BY","admin1":"Минск",
                                "timezone":"Europe/Minsk"}]}
                                """)));

        SERVER.stubFor(
                get(urlPathEqualTo("/v1/forecast"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {"timezone":"Europe/Minsk",
                                "current":{"temperature_2m":17.9,"apparent_temperature":16.0,
                                "relative_humidity_2m":50,"weather_code":3,"wind_speed_10m":8.6},
                                "daily":{"time":["2026-09-13"],"weather_code":[3],
                                "temperature_2m_max":[18.7],"temperature_2m_min":[10.8]}}
                                """)));
    }

    @Test
    void repeatedLookupOfSameCityDoesNotHitTheNetworkTwice() {
        assertThat(weatherService.forecastFor("Минск")).isPresent();
        assertThat(weatherService.forecastFor("Минск")).isPresent();
        assertThat(weatherService.forecastFor("Минск")).isPresent();

        SERVER.verify(1, getRequestedFor(urlPathEqualTo("/v1/search")));
        SERVER.verify(1, getRequestedFor(urlPathEqualTo("/v1/forecast")));
    }

    @Test
    void differentCitiesAreLookedUpSeparately() {
        weatherService.findPlace("Минск");
        weatherService.findPlace("Гродно");

        SERVER.verify(2, getRequestedFor(urlPathEqualTo("/v1/search")));
    }
}
