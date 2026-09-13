package ru.zagvladimir.tgbot.currency.internal;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CurrencyProperties.class)
class CurrencyClientConfiguration {

    @Bean
    NbrbApi nbrbApi(RestClient.Builder builder, CurrencyProperties properties) {
        return createClient(builder, properties);
    }

    static NbrbApi createClient(RestClient.Builder builder, CurrencyProperties properties) {
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
