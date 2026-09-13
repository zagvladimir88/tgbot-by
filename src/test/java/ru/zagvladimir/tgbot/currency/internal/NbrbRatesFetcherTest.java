package ru.zagvladimir.tgbot.currency.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class NbrbRatesFetcherTest {

    private static final String RATES_BODY =
            """
            [
              {"Cur_ID":431,"Date":"2026-09-13T00:00:00","Cur_Abbreviation":"USD",
               "Cur_Scale":1,"Cur_Name":"Доллар США","Cur_OfficialRate":3.2841},
              {"Cur_ID":451,"Date":"2026-09-13T00:00:00","Cur_Abbreviation":"EUR",
               "Cur_Scale":1,"Cur_Name":"Евро","Cur_OfficialRate":3.8412},
              {"Cur_ID":456,"Date":"2026-09-13T00:00:00","Cur_Abbreviation":"RUB",
               "Cur_Scale":100,"Cur_Name":"Российских рублей","Cur_OfficialRate":3.8912},
              {"Cur_ID":508,"Date":"2026-09-13T00:00:00","Cur_Abbreviation":"JPY",
               "Cur_Scale":100,"Cur_Name":"Японских иен","Cur_OfficialRate":2.0500}
            ]
            """;

    private WireMockServer server;

    @BeforeEach
    void startServer() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void normalizesRatesByCurScale() {
        stubRates();

        var snapshot = fetcher().fetchDaily();

        assertThat(snapshot.rate("USD")).get().satisfies(usd -> assertThat(usd.ratePerUnit())
                .isEqualByComparingTo("3.2841"));

        assertThat(snapshot.rate("JPY")).get().satisfies(jpy -> assertThat(jpy.ratePerUnit())
                .isEqualByComparingTo("0.0205"));

        assertThat(snapshot.rate("RUB")).get().satisfies(rub -> assertThat(rub.ratePerUnit())
                .isEqualByComparingTo("0.038912"));
    }

    @Test
    void readsSnapshotDateFromResponse() {
        stubRates();

        assertThat(fetcher().fetchDaily().onDate()).isEqualTo(LocalDate.of(2026, 9, 13));
    }

    @Test
    void isCaseInsensitiveOnCurrencyLookup() {
        stubRates();

        var snapshot = fetcher().fetchDaily();

        assertThat(snapshot.rate("usd")).isPresent();
        assertThat(snapshot.supports("byn")).isTrue();
        assertThat(snapshot.supports("XXX")).isFalse();
    }

    @Test
    void rejectsEmptyResponse() {
        server.stubFor(get(urlPathEqualTo("/exrates/rates"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        assertThatThrownBy(() -> fetcher().fetchDaily())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("пустой список");
    }

    @Test
    void rejectsNonPositiveScaleInsteadOfDividingByZero() {
        var broken = new NbrbRateDto(1, null, "XXX", 0, "Сломанная", new BigDecimal("1.5"));

        assertThatThrownBy(() -> NbrbRatesFetcher.normalize(broken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cur_Scale");
    }

    private void stubRates() {
        server.stubFor(get(urlPathEqualTo("/exrates/rates"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(RATES_BODY)));
    }

    private NbrbRatesFetcher fetcher() {
        var properties = new CurrencyProperties(server.baseUrl(), null);
        var api = CurrencyClientConfiguration.createClient(RestClient.builder(), properties);
        return new NbrbRatesFetcher(api, Clock.fixed(Instant.parse("2026-09-13T10:00:00Z"), ZoneId.of("UTC")));
    }
}
