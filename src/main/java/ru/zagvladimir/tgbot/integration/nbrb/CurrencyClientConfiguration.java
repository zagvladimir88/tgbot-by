package ru.zagvladimir.tgbot.integration.nbrb;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.zagvladimir.tgbot.app.properties.CurrencyProperties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CurrencyProperties.class)
public class CurrencyClientConfiguration {

    @Bean
    public NbrbApi nbrbApi(RestClient.Builder builder, CurrencyProperties properties) {
        return createClient(builder, properties);
    }

    public static NbrbApi createClient(RestClient.Builder builder, CurrencyProperties properties) {
        var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newHttpClient());
        requestFactory.setReadTimeout(properties.timeout());

        var restClient = builder.clone()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(NbrbApi.class);
    }
}
