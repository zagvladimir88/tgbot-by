package ru.zagvladimir.tgbot.telegram.format;

public final class HtmlEscaper {

    private HtmlEscaper() {}

    public static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
