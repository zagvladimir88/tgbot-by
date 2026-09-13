package ru.zagvladimir.tgbot.telegram.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CommandRegistryTest {

    @Test
    void findsHandlerByEveryDeclaredAlias() {
        var handler = new StubHandler(Set.of("/w", "/weather"));
        var registry = new CommandRegistry(List.of(handler));

        assertThat(registry.find("/w")).containsSame(handler);
        assertThat(registry.find("/weather")).containsSame(handler);
        assertThat(registry.find("/unknown")).isEmpty();
    }

    @Test
    void rejectsDuplicateCommandsAtStartup() {
        var first = new StubHandler(Set.of("/w"));
        var second = new StubHandler(Set.of("/w"));

        assertThatThrownBy(() -> new CommandRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("/w");
    }

    private static final class StubHandler implements CommandHandler {

        private final Set<String> commands;

        private StubHandler(Set<String> commands) {
            this.commands = commands;
        }

        @Override
        public Set<String> commands() {
            return commands;
        }

        @Override
        public String description() {
            return "заглушка";
        }

        @Override
        public void handle(CommandContext context) {}
    }
}
