package ru.zagvladimir.tgbot.telegram.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.domain.image.ImageSearchService;
import ru.zagvladimir.tgbot.domain.image.model.ImageResult;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchOutcome;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchPage;

class ImageSearchPresenterTest {

    private final ImageSearchService searchService = mock(ImageSearchService.class);
    private final ImageSearchPresenter presenter = new ImageSearchPresenter(searchService);

    @Test
    void remembersQueryUnderShortIdentifier() {
        var id = presenter.rememberQuery("рыжий кот");

        assertThat(id).isNotBlank();
        assertThat(presenter.queryById(id)).isEqualTo("рыжий кот");
    }

    @Test
    void callbackDataFitsTelegramLimit() {
        var id = presenter.rememberQuery("очень длинный поисковый запрос про котиков и собачек тоже");

        var keyboard = presenter.keyboard(id, 5, true);

        for (var row : keyboard.getKeyboard()) {
            for (var button : row) {
                assertThat(button.getCallbackData().getBytes(StandardCharsets.UTF_8))
                        .hasSizeLessThanOrEqualTo(64);
            }
        }
    }

    @Test
    void unknownIdentifierReturnsNull() {
        assertThat(presenter.queryById("deadbeef")).isNull();
    }

    @Test
    void firstPageHasNoBackButton() {
        var id = presenter.rememberQuery("кот");

        var labels = labels(id, 1, true);

        assertThat(labels).doesNotContain("◀").contains("▶");
    }

    @Test
    void laterPagesHaveBothDirections() {
        var id = presenter.rememberQuery("кот");

        assertThat(labels(id, 5, true)).contains("◀", "▶");
    }

    @Test
    void translatesPageIndexIntoSearchOffset() {
        when(searchService.search("кот", 11)).thenReturn(new ImageSearchOutcome.NothingFound("кот"));

        presenter.searchAt("кот", 12);

        org.mockito.Mockito.verify(searchService).search("кот", 11);
    }

    @Test
    void picksImageByPositionInsidePage() {
        var found = new ImageSearchOutcome.Found(new ImageSearchPage("кот", 1, List.of(image("a"), image("b")), true));

        assertThat(presenter.pick(found, 2).imageUrl()).isEqualTo("b");
        assertThat(presenter.pick(found, 9)).isNull();
    }

    @Test
    void describesEveryFailureDistinctly() {
        assertThat(ImageSearchPresenter.describe(new ImageSearchOutcome.NothingFound("кот")))
                .contains("Ничего не нашёл");
        assertThat(ImageSearchPresenter.describe(new ImageSearchOutcome.QuotaExceeded(100)))
                .contains("лимит");
        assertThat(ImageSearchPresenter.describe(new ImageSearchOutcome.Unavailable(null)))
                .contains("недоступен");
        assertThat(ImageSearchPresenter.describe(new ImageSearchOutcome.Unavailable("нет ключа")))
                .contains("нет ключа");
    }

    @Test
    void captionEscapesHtmlFromSearchResults() {
        var caption = presenter.caption("кот", image("https://e.org/1.jpg", "<b>злой</b>"), 3);

        assertThat(caption).contains("&lt;b&gt;").doesNotContain("<b>злой");
    }

    private List<String> labels(String queryId, int index, boolean hasMore) {
        return presenter.keyboard(queryId, index, hasMore).getKeyboard().stream()
                .flatMap(List::stream)
                .map(button -> button.getText())
                .toList();
    }

    private static ImageResult image(String url) {
        return image(url, "Картинка");
    }

    private static ImageResult image(String url, String title) {
        return new ImageResult(title, url, url, url, 800, 600);
    }
}
