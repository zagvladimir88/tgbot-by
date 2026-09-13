package ru.zagvladimir.tgbot.telegram.sender;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SendThrottleTest {

    private final SendThrottle throttle = new SendThrottle();

    @Test
    void allowsBurstWithinLimitsWithoutDelay() {
        var start = Instant.now();

        for (var i = 0; i < 10; i++) {
            throttle.acquire(100L);
        }

        assertThat(Duration.between(start, Instant.now())).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void throttlesWhenChatLimitIsExhausted() {
        for (var i = 0; i < 18; i++) {
            throttle.acquire(200L);
        }

        var start = Instant.now();
        throttle.acquire(200L);

        assertThat(Duration.between(start, Instant.now())).isGreaterThan(Duration.ofMillis(500));
    }

    @Test
    void limitsAreCountedPerChat() {
        for (var i = 0; i < 18; i++) {
            throttle.acquire(300L);
        }

        var start = Instant.now();
        throttle.acquire(301L);

        assertThat(Duration.between(start, Instant.now())).isLessThan(Duration.ofMillis(500));
    }
}
