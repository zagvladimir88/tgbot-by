package ru.zagvladimir.tgbot.currency.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;
import ru.zagvladimir.tgbot.currency.CurrencyRate;
import ru.zagvladimir.tgbot.currency.CurrencyService;
import ru.zagvladimir.tgbot.currency.RateSnapshot;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "bot.currency.timeout=2s")
class CurrencyServiceIntegrationTest {

    private static final WireMockServer SERVER =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        SERVER.start();
    }

    @DynamicPropertySource
    static void nbrbUrl(DynamicPropertyRegistry registry) {
        registry.add("bot.currency.base-url", SERVER::baseUrl);
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop();
    }

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private RateSnapshotStore store;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void resetState() {
        SERVER.resetAll();
        jdbcClient.sql("delete from rate_snapshot").update();
    }

    @Test
    void convertsThroughBaseCurrencyUsingCrossRate() {
        stubRates(ratesBody("2026-09-13", "3.2841", "3.8412"));

        var conversion = currencyService.convert(new BigDecimal("100"), "usd", "eur");

        assertThat(conversion).get().satisfies(result -> {
            assertThat(result.from()).isEqualTo("USD");
            assertThat(result.to()).isEqualTo("EUR");
            assertThat(result.result())
                    .isCloseTo(new BigDecimal("85.4967"), org.assertj.core.data.Offset.offset(new BigDecimal("0.001")));
        });
    }

    @Test
    void convertsToBaseCurrencyRespectingScale() {
        stubRates(ratesBody("2026-09-13", "3.2841", "3.8412"));

        var conversion = currencyService.convert(new BigDecimal("1000"), "jpy", "byn");

        assertThat(conversion).get().satisfies(result -> assertThat(result.result())
                .isCloseTo(new BigDecimal("20.50"), org.assertj.core.data.Offset.offset(new BigDecimal("0.01"))));
    }

    @Test
    void rejectsUnknownCurrency() {
        stubRates(ratesBody("2026-09-13", "3.2841", "3.8412"));

        assertThat(currencyService.convert(new BigDecimal("100"), "usd", "xxx")).isEmpty();
    }

    @Test
    void servesLastStoredSnapshotWhenNbrbIsDown() {
        stubRates(ratesBody("2026-09-13", "3.2841", "3.8412"));
        assertThat(currencyService.latestRates()).isPresent();

        SERVER.resetAll();
        SERVER.stubFor(
                get(urlPathEqualTo("/exrates/rates")).willReturn(aResponse().withStatus(503)));

        var snapshot = currencyService.latestRates();

        assertThat(snapshot).get().satisfies(stored -> {
            assertThat(stored.onDate()).isEqualTo(LocalDate.of(2026, 9, 13));
            assertThat(stored.rate("USD")).isPresent();
        });
    }

    @Test
    void computesDeltaAgainstPreviousDay() {
        store.save(snapshot(LocalDate.of(2026, 9, 12), "3.2800"));
        store.save(snapshot(LocalDate.of(2026, 9, 13), "3.2841"));

        var rates = currencyService.ratesWithDelta(List.of("USD"));

        assertThat(rates).singleElement().satisfies(entry -> {
            assertThat(entry.rate().code()).isEqualTo("USD");
            assertThat(entry.grew()).isTrue();
            assertThat(entry.delta()).isEqualByComparingTo("0.0041");
        });
    }

    @Test
    void reportsNoDeltaWhenThereIsNoPreviousSnapshot() {
        store.save(snapshot(LocalDate.of(2026, 9, 13), "3.2841"));

        var rates = currencyService.ratesWithDelta(List.of("USD"));

        assertThat(rates).singleElement().satisfies(entry -> {
            assertThat(entry.delta()).isNull();
            assertThat(entry.grew()).isFalse();
            assertThat(entry.fell()).isFalse();
        });
    }

    private static RateSnapshot snapshot(LocalDate onDate, String usdRate) {
        return new RateSnapshot(
                onDate,
                Instant.parse("2026-09-13T10:00:00Z"),
                Map.of("USD", new CurrencyRate("USD", "Доллар США", new BigDecimal(usdRate))));
    }

    private static void stubRates(String body) {
        SERVER.stubFor(get(urlPathEqualTo("/exrates/rates"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private static String ratesBody(String date, String usd, String eur) {
        return """
                [
                  {"Cur_ID":431,"Date":"%s T00:00:00","Cur_Abbreviation":"USD",
                   "Cur_Scale":1,"Cur_Name":"Доллар США","Cur_OfficialRate":%s},
                  {"Cur_ID":451,"Date":"%s T00:00:00","Cur_Abbreviation":"EUR",
                   "Cur_Scale":1,"Cur_Name":"Евро","Cur_OfficialRate":%s},
                  {"Cur_ID":508,"Date":"%s T00:00:00","Cur_Abbreviation":"JPY",
                   "Cur_Scale":100,"Cur_Name":"Японских иен","Cur_OfficialRate":2.0500}
                ]
                """
                .formatted(date, usd, date, eur, date)
                .replace(" T00", "T00");
    }
}
