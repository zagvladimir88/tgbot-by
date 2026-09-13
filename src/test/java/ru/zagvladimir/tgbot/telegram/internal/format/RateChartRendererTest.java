package ru.zagvladimir.tgbot.telegram.internal.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import ru.zagvladimir.tgbot.currency.RateHistoryPoint;

class RateChartRendererTest {

    private final RateChartRenderer renderer = new RateChartRenderer();

    @Test
    void rendersReadablePngOfExpectedSize() throws Exception {
        var png = renderer.render("USD", history());

        assertThat(png).isNotEmpty();

        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(900);
        assertThat(image.getHeight()).isEqualTo(500);
    }

    @Test
    void startsWithPngSignature() {
        var png = renderer.render("EUR", history());

        assertThat(png[0]).isEqualTo((byte) 0x89);
        assertThat(new String(png, 1, 3, java.nio.charset.StandardCharsets.US_ASCII))
                .isEqualTo("PNG");
    }

    private static List<RateHistoryPoint> history() {
        return List.of(
                new RateHistoryPoint(LocalDate.of(2026, 8, 15), new BigDecimal("3.2500")),
                new RateHistoryPoint(LocalDate.of(2026, 8, 22), new BigDecimal("3.2700")),
                new RateHistoryPoint(LocalDate.of(2026, 9, 1), new BigDecimal("3.2610")),
                new RateHistoryPoint(LocalDate.of(2026, 9, 13), new BigDecimal("3.2841")));
    }
}
