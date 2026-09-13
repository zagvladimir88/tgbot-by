package ru.zagvladimir.tgbot.infra.persistence;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zagvladimir.tgbot.domain.settings.ChatSettingsService;
import ru.zagvladimir.tgbot.domain.settings.model.ChatSettings;

@Service
public class JdbcChatSettingsService implements ChatSettingsService {

    private final JdbcClient jdbcClient;

    JdbcChatSettingsService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatSettings find(long chatId) {
        return jdbcClient
                .sql("select chat_id, default_city, default_currencies, zone_id from chat_settings where chat_id = ?")
                .param(chatId)
                .query((rs, rowNum) -> new ChatSettings(
                        rs.getLong("chat_id"),
                        rs.getString("default_city"),
                        toList(rs.getArray("default_currencies")),
                        ZoneId.of(rs.getString("zone_id"))))
                .optional()
                .orElseGet(() -> ChatSettings.defaults(chatId));
    }

    @Override
    @Transactional
    public void setDefaultCity(long chatId, String city) {
        jdbcClient
                .sql(
                        """
                        insert into chat_settings (chat_id, default_city)
                        values (?, ?)
                        on conflict (chat_id) do update
                        set default_city = excluded.default_city, updated_at = now()
                        """)
                .param(chatId)
                .param(city)
                .update();
    }

    @Override
    @Transactional
    public void setDefaultCurrencies(long chatId, List<String> currencies) {
        var normalized = currencies.stream()
                .map(currency -> currency.toUpperCase(Locale.ROOT))
                .distinct()
                .toArray(String[]::new);

        jdbcClient
                .sql(
                        """
                        insert into chat_settings (chat_id, default_currencies)
                        values (?, ?)
                        on conflict (chat_id) do update
                        set default_currencies = excluded.default_currencies, updated_at = now()
                        """)
                .param(chatId)
                .param(normalized)
                .update();
    }

    private static List<String> toList(java.sql.Array array) throws java.sql.SQLException {
        if (array == null) {
            return ChatSettings.DEFAULT_CURRENCIES;
        }
        return List.copyOf(Arrays.asList((String[]) array.getArray()));
    }
}
