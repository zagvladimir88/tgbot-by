package ru.zagvladimir.tgbot.domain.layout;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LayoutConverterTest {

    private final LayoutConverter converter = new LayoutConverter();

    @ParameterizedTest
    @CsvSource({"ghbdtn, привет", "rfr ltkf, как дела", "Ghbdtn, Привет", "vfvf vskf hfve, мама мыла раму", "ntcn, тест"
    })
    void convertsLatinKeystrokesToCyrillic(String typed, String expected) {
        assertThat(converter.toCyrillic(typed)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"привет, ghbdtn", "как дела, rfr ltkf", "Привет, Ghbdtn", "тест, ntcn"})
    void convertsCyrillicKeystrokesToLatin(String typed, String expected) {
        assertThat(converter.toLatin(typed)).isEqualTo(expected);
    }

    @Test
    void convertsPunctuationLayer() {
        assertThat(converter.toCyrillic(";'")).isEqualTo("жэ");
        assertThat(converter.toCyrillic("[]")).isEqualTo("хъ");
        assertThat(converter.toCyrillic(",.")).isEqualTo("бю");
        assertThat(converter.toCyrillic("/")).isEqualTo(".");
        assertThat(converter.toCyrillic("`")).isEqualTo("ё");
    }

    @Test
    void convertsShiftedPunctuationLayer() {
        assertThat(converter.toCyrillic(":\"")).isEqualTo("ЖЭ");
        assertThat(converter.toCyrillic("{}")).isEqualTo("ХЪ");
        assertThat(converter.toCyrillic("<>")).isEqualTo("БЮ");
        assertThat(converter.toCyrillic("?")).isEqualTo(",");
        assertThat(converter.toCyrillic("~")).isEqualTo("Ё");
    }

    @Test
    void convertsCyrillicPunctuationBack() {
        assertThat(converter.toLatin("жэ")).isEqualTo(";'");
        assertThat(converter.toLatin("хъ")).isEqualTo("[]");
        assertThat(converter.toLatin("бю")).isEqualTo(",.");
        assertThat(converter.toLatin(".")).isEqualTo("/");
        assertThat(converter.toLatin("ё")).isEqualTo("`");
    }

    @Test
    void detectsDirectionAutomatically() {
        assertThat(converter.convert("ghbdtn")).isEqualTo("привет");
        assertThat(converter.convert("привет")).isEqualTo("ghbdtn");
    }

    @Test
    void picksDirectionByDominantAlphabetInMixedText() {
        assertThat(converter.convert("ghbdtn мир")).isEqualTo("привет мир");
        assertThat(converter.convert("привет vbh")).isEqualTo("ghbdtn vbh");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123 456", "!@#", "", "   "})
    void leavesTextWithoutLettersUntouched(String text) {
        assertThat(converter.toCyrillic(converter.toLatin(text))).isEqualTo(text);
    }

    @Test
    void keepsCharactersOutsideBothLayouts() {
        assertThat(converter.toCyrillic("ghbdtn 2024 😀")).isEqualTo("привет 2024 😀");
    }

    @Test
    void roundTripRestoresLettersAndSpaces() {
        var original = "Привет как дела Всё хорошо";

        assertThat(converter.toCyrillic(converter.toLatin(original))).isEqualTo(original);
    }

    @Test
    void questionMarkIsNotReversibleBecauseLayoutsAreNotBijective() {
        assertThat(converter.toLatin("?")).isEqualTo("?");
        assertThat(converter.toCyrillic("?")).isEqualTo(",");
    }

    @Test
    void everyLatinLetterHasCyrillicCounterpart() {
        var latin = "abcdefghijklmnopqrstuvwxyz";

        var converted = converter.toCyrillic(latin);

        assertThat(converted).hasSize(latin.length());
        assertThat(converted.chars().anyMatch(c -> c >= 'a' && c <= 'z')).isFalse();
    }
}
