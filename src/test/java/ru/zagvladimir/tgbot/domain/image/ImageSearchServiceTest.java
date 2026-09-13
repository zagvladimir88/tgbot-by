package ru.zagvladimir.tgbot.domain.image;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.zagvladimir.tgbot.TestcontainersConfiguration;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchOutcome;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"bot.image.api-key=test-key", "bot.image.cx=test-cx", "bot.image.daily-limit=3"})
class ImageSearchServiceTest {

    private static final String RESPONSE_BODY =
            """
            {
              "items": [
                {
                  "title": "Котик",
                  "link": "https://example.org/cat.jpg",
                  "image": {
                    "contextLink": "https://example.org/page",
                    "thumbnailLink": "https://example.org/thumb.jpg",
                    "width": 800,
                    "height": 600
                  }
                }
              ],
              "queries": {"nextPage": [{"startIndex": 11}]}
            }
            """;

    private static final WireMockServer SERVER =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        SERVER.start();
    }

    @DynamicPropertySource
    static void googleUrl(DynamicPropertyRegistry registry) {
        registry.add("bot.image.base-url", SERVER::baseUrl);
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop();
    }

    @Autowired
    private ImageSearchService searchService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void resetState() {
        SERVER.resetAll();
        jdbcClient.sql("delete from cse_quota").update();
        cacheManager.getCache("image-search").clear();

        SERVER.stubFor(get(urlPathEqualTo("/customsearch/v1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_BODY)));
    }

    @Test
    void mapsGoogleResponseToImageResults() {
        var outcome = searchService.search("котики", 1);

        assertThat(outcome).isInstanceOfSatisfying(ImageSearchOutcome.Found.class, found -> {
            assertThat(found.page().results()).singleElement().satisfies(image -> {
                assertThat(image.title()).isEqualTo("Котик");
                assertThat(image.imageUrl()).isEqualTo("https://example.org/cat.jpg");
                assertThat(image.thumbnailUrl()).isEqualTo("https://example.org/thumb.jpg");
                assertThat(image.width()).isEqualTo(800);
            });
            assertThat(found.page().hasMore()).isTrue();
        });
    }

    @Test
    void repeatedQueryIsServedFromCacheWithoutSpendingQuota() {
        searchService.search("котики", 1);
        searchService.search("котики", 1);
        searchService.search("котики", 1);

        SERVER.verify(1, getRequestedFor(urlPathEqualTo("/customsearch/v1")));
    }

    @Test
    void reportsQuotaExceededInsteadOfFailingWithApiError() {
        searchService.search("запрос1", 1);
        searchService.search("запрос2", 1);
        searchService.search("запрос3", 1);

        var outcome = searchService.search("запрос4", 1);

        assertThat(outcome)
                .isInstanceOfSatisfying(ImageSearchOutcome.QuotaExceeded.class, quota -> assertThat(quota.dailyLimit())
                        .isEqualTo(3));
    }

    @Test
    void reportsNothingFoundOnEmptyItems() {
        SERVER.resetAll();
        SERVER.stubFor(get(urlPathEqualTo("/customsearch/v1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        assertThat(searchService.search("несуществующее", 1)).isInstanceOf(ImageSearchOutcome.NothingFound.class);
    }

    @Test
    void reportsUnavailableWhenGoogleFails() {
        SERVER.resetAll();
        SERVER.stubFor(
                get(urlPathEqualTo("/customsearch/v1")).willReturn(aResponse().withStatus(500)));

        assertThat(searchService.search("что-нибудь", 1)).isInstanceOf(ImageSearchOutcome.Unavailable.class);
    }
}
