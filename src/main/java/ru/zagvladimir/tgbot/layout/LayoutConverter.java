package ru.zagvladimir.tgbot.layout;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LayoutConverter {

    private static final String LATIN = "qwertyuiop[]asdfghjkl;'zxcvbnm,./`" + "QWERTYUIOP{}ASDFGHJKL:\"ZXCVBNM<>?~";

    private static final String CYRILLIC = "йцукенгшщзхъфывапролджэячсмитьбю.ё" + "ЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ,Ё";

    private static final Map<Character, Character> LATIN_TO_CYRILLIC = buildMap(LATIN, CYRILLIC);
    private static final Map<Character, Character> CYRILLIC_TO_LATIN = buildMap(CYRILLIC, LATIN);

    private static Map<Character, Character> buildMap(String from, String to) {
        if (from.length() != to.length()) {
            throw new IllegalStateException("Раскладки разной длины: %d и %d".formatted(from.length(), to.length()));
        }

        Map<Character, Character> map = new HashMap<>(from.length());
        for (var i = 0; i < from.length(); i++) {
            map.put(from.charAt(i), to.charAt(i));
        }
        return Map.copyOf(map);
    }

    public String convert(String text) {
        return looksCyrillic(text) ? toLatin(text) : toCyrillic(text);
    }

    public String toCyrillic(String text) {
        return translate(text, LATIN_TO_CYRILLIC);
    }

    public String toLatin(String text) {
        return translate(text, CYRILLIC_TO_LATIN);
    }

    private static String translate(String text, Map<Character, Character> mapping) {
        var result = new StringBuilder(text.length());
        for (var i = 0; i < text.length(); i++) {
            var source = text.charAt(i);
            result.append(mapping.getOrDefault(source, source));
        }
        return result.toString();
    }

    private static boolean looksCyrillic(String text) {
        var cyrillic = 0;
        var latin = 0;
        for (var i = 0; i < text.length(); i++) {
            var character = text.charAt(i);
            if (Character.UnicodeBlock.of(character) == Character.UnicodeBlock.CYRILLIC) {
                cyrillic++;
            } else if (character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z') {
                latin++;
            }
        }
        return cyrillic > latin;
    }
}
