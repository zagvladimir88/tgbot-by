package ru.zagvladimir.tgbot.telegram.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.subscription.model.SubscriptionType;

class SubscribeCommandHandlerTest {

    @Test
    void buildsDailyCronFromTime() {
        assertThat(SubscribeCommandHandler.toCron(LocalTime.of(9, 0))).isEqualTo("0 0 9 * * *");
        assertThat(SubscribeCommandHandler.toCron(LocalTime.of(10, 30))).isEqualTo("0 30 10 * * *");
        assertThat(SubscribeCommandHandler.toCron(LocalTime.of(0, 5))).isEqualTo("0 5 0 * * *");
    }

    @Test
    void parsesKnownSubscriptionTypes() {
        assertThat(SubscribeCommandHandler.parseType("weather")).isEqualTo(SubscriptionType.WEATHER);
        assertThat(SubscribeCommandHandler.parseType("RATES")).isEqualTo(SubscriptionType.RATES);
        assertThat(SubscribeCommandHandler.parseType("digest")).isEqualTo(SubscriptionType.DIGEST);
        assertThat(SubscribeCommandHandler.parseType("погода")).isNull();
    }

    @Test
    void parsesTimeAndRejectsGarbage() {
        assertThat(SubscribeCommandHandler.parseTime("09:00")).contains(LocalTime.of(9, 0));
        assertThat(SubscribeCommandHandler.parseTime("25:00")).isEmpty();
        assertThat(SubscribeCommandHandler.parseTime("утром")).isEmpty();
    }
}
