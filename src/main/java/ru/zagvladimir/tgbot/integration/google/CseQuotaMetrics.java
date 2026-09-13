package ru.zagvladimir.tgbot.integration.google;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.app.properties.ImageProperties;
import ru.zagvladimir.tgbot.domain.image.port.SearchQuotaPort;

@Component
public class CseQuotaMetrics {

    private final MeterRegistry registry;
    private final SearchQuotaPort quota;
    private final ImageProperties properties;

    CseQuotaMetrics(MeterRegistry registry, SearchQuotaPort quota, ImageProperties properties) {
        this.registry = registry;
        this.quota = quota;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    void registerGauges() {
        Gauge.builder("bot.image.quota.used", quota, SearchQuotaPort::usedToday)
                .description("Израсходовано запросов Google CSE за сутки")
                .register(registry);

        Gauge.builder("bot.image.quota.remaining", quota, port -> properties.dailyLimit() - port.usedToday())
                .description("Остаток суточной квоты Google CSE")
                .register(registry);
    }
}
