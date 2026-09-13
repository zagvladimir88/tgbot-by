package ru.zagvladimir.tgbot.integration.nbrb;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;
import ru.zagvladimir.tgbot.domain.currency.port.RatesPort;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NbrbRetryTest {

    private static final String RATES_BODY =
            """
            [
              {"Cur_ID":431,"Date":"2026-09-13T00:00:00","Cur_Abbreviation":"USD",
               "Cur_Scale":1,"Cur_Name":"Доллар США","Cur_OfficialRate":3.2841}
            ]
            """;

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
    private RatesPort ratesPort;

    @BeforeEach
    void resetServer() {
        SERVER.resetAll();
    }

    @Test
    void recoversAfterTwoServerErrors() {
        SERVER.stubFor(get(urlPathEqualTo("/exrates/rates"))
                .inScenario("flaky")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("second"));

        SERVER.stubFor(get(urlPathEqualTo("/exrates/rates"))
                .inScenario("flaky")
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("third"));

        SERVER.stubFor(get(urlPathEqualTo("/exrates/rates"))
                .inScenario("flaky")
                .whenScenarioStateIs("third")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(RATES_BODY)));

        var snapshot = ratesPort.fetchDaily();

        assertThat(snapshot.rate("USD")).isPresent();
        SERVER.verify(3, getRequestedFor(urlPathEqualTo("/exrates/rates")));
    }

    @Test
    void givesUpAfterRetriesAreExhausted() {
        SERVER.stubFor(
                get(urlPathEqualTo("/exrates/rates")).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> ratesPort.fetchDaily()).isInstanceOf(RuntimeException.class);

        SERVER.verify(3, getRequestedFor(urlPathEqualTo("/exrates/rates")));
    }

    @Test
    void doesNotRetryClientErrors() {
        SERVER.stubFor(
                get(urlPathEqualTo("/exrates/rates")).willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> ratesPort.fetchDaily()).isInstanceOf(RuntimeException.class);

        SERVER.verify(1, getRequestedFor(urlPathEqualTo("/exrates/rates")));
    }
}
