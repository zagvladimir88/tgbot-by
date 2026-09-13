package ru.zagvladimir.tgbot;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfiguration {

    @Bean
    CacheManager cacheManager() {
        var manager = new CaffeineCacheManager();

        manager.registerCustomCache(
                "geocoding",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofDays(30))
                        .maximumSize(1_000)
                        .build());

        manager.registerCustomCache(
                "forecast",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(500)
                        .build());

        manager.registerCustomCache(
                "currency-directory",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofDays(1))
                        .maximumSize(10)
                        .build());

        return manager;
    }
}
