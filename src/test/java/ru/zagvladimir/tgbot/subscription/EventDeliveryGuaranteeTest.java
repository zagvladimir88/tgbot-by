package ru.zagvladimir.tgbot.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;
import ru.zagvladimir.tgbot.domain.currency.CurrencyService;
import ru.zagvladimir.tgbot.domain.weather.WeatherService;
import ru.zagvladimir.tgbot.subscription.event.SubscriptionDue;
import ru.zagvladimir.tgbot.subscription.model.SubscriptionType;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EventDeliveryGuaranteeTest {

    private static final long CHAT_ID = 777L;

    @MockitoBean
    private MessageSender sender;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private CurrencyService currencyService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private IncompleteEventPublications incomplete;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void clean() {
        jdbcClient.sql("delete from event_publication").update();
        org.mockito.Mockito.when(currencyService.latestRates()).thenReturn(Optional.empty());
        org.mockito.Mockito.when(currencyService.ratesWithDelta(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of());
    }

    @Test
    void successfulBroadcastLeavesNoOutstandingPublications() {
        publishDue();

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> verify(sender, atLeastOnce()).sendHtml(anyLong(), anyString()));

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(outstanding()).isZero());
    }

    @Test
    void failedBroadcastStaysInRegistryAndIsDeliveredOnReplay() {
        doThrow(new IllegalStateException("Telegram недоступен")).when(sender).sendHtml(anyLong(), anyString());

        publishDue();

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(outstanding()).isEqualTo(1));

        doNothing().when(sender).sendHtml(anyLong(), anyString());
        incomplete.resubmitIncompletePublications(publication -> true);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(outstanding()).isZero());
    }

    private void publishDue() {
        transactionTemplate.executeWithoutResult(
                status -> publisher.publishEvent(new SubscriptionDue(1L, CHAT_ID, SubscriptionType.RATES, "")));
    }

    private int outstanding() {
        return jdbcClient
                .sql("select count(*) from event_publication where completion_date is null")
                .query(Integer.class)
                .single();
    }
}
