package ru.zagvladimir.tgbot.integration.openmeteo;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.zagvladimir.tgbot.app.properties.WeatherProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WeatherProperties.class)
public class WeatherClientConfiguration {

    @Bean
    public OpenMeteoGeocodingApi openMeteoGeocodingApi(RestClient.Builder builder, WeatherProperties properties) {
        return createClient(builder, properties.geocodingUrl(), properties, OpenMeteoGeocodingApi.class);
    }

    @Bean
    public OpenMeteoForecastApi openMeteoForecastApi(RestClient.Builder builder, WeatherProperties properties) {
        return createClient(builder, properties.forecastUrl(), properties, OpenMeteoForecastApi.class);
    }

    public static <T> T createClient(
            RestClient.Builder builder, String baseUrl, WeatherProperties properties, Class<T> type) {
        var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newHttpClient());
        requestFactory.setReadTimeout(properties.timeout());

        var restClient =
                builder.clone().baseUrl(baseUrl).requestFactory(requestFactory).build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(type);
    }
}
