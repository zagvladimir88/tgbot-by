package ru.zagvladimir.tgbot.app;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        var manager = new CaffeineCacheManager();

        manager.registerCustomCache(
                "geocoding",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofDays(30))
                        .recordStats()
                        .maximumSize(1_000)
                        .build());

        manager.registerCustomCache(
                "forecast",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .recordStats()
                        .maximumSize(500)
                        .build());

        manager.registerCustomCache(
                "currency-directory",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofDays(1))
                        .recordStats()
                        .maximumSize(10)
                        .build());

        manager.registerCustomCache(
                "image-search",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(24))
                        .recordStats()
                        .maximumSize(2_000)
                        .build());

        return manager;
    }
}
