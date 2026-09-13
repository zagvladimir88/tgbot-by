package ru.zagvladimir.tgbot.integration.google;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.zagvladimir.tgbot.app.properties.ImageProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ImageProperties.class)
public class ImageClientConfiguration {

    @Bean
    public GoogleCseApi googleCseApi(RestClient.Builder builder, ImageProperties properties) {
        return createClient(builder, properties);
    }

    public static GoogleCseApi createClient(RestClient.Builder builder, ImageProperties properties) {
        var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newHttpClient());
        requestFactory.setReadTimeout(properties.timeout());

        var restClient = builder.clone()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(GoogleCseApi.class);
    }
}
