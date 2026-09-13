package ru.zagvladimir.tgbot.domain.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ChatSettingsServiceTest {

    @Autowired
    private ChatSettingsService service;

    @Test
    void returnsDefaultsForUnknownChat() {
        var settings = service.find(-1L);

        assertThat(settings.defaultCity()).isNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.of("Europe/Minsk"));
        assertThat(settings.defaultCurrencies()).containsExactly("USD", "EUR", "RUB");
    }

    @Test
    void storesAndReadsBackDefaultCity() {
        service.setDefaultCity(1001L, "Гродно");

        assertThat(service.find(1001L).defaultCity()).isEqualTo("Гродно");
    }

    @Test
    void overwritesCityOnRepeatedCall() {
        service.setDefaultCity(1002L, "Минск");
        service.setDefaultCity(1002L, "Брест");

        assertThat(service.find(1002L).defaultCity()).isEqualTo("Брест");
    }

    @Test
    void storesCurrenciesUppercasedAndDeduplicated() {
        service.setDefaultCurrencies(1003L, List.of("usd", "USD", "pln"));

        assertThat(service.find(1003L).defaultCurrencies()).containsExactly("USD", "PLN");
    }

    @Test
    void keepsCityWhenOnlyCurrenciesChange() {
        service.setDefaultCity(1004L, "Витебск");
        service.setDefaultCurrencies(1004L, List.of("EUR"));

        var settings = service.find(1004L);

        assertThat(settings.defaultCity()).isEqualTo("Витебск");
        assertThat(settings.defaultCurrencies()).containsExactly("EUR");
    }
}
