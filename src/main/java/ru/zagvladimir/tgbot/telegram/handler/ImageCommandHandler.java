package ru.zagvladimir.tgbot.telegram.handler;

import java.util.Set;
import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.domain.image.model.ImageSearchOutcome;
import ru.zagvladimir.tgbot.telegram.command.CommandContext;
import ru.zagvladimir.tgbot.telegram.command.CommandHandler;
import ru.zagvladimir.tgbot.telegram.presenter.ImageSearchPresenter;
import ru.zagvladimir.tgbot.telegram.sender.MessageSender;

@Component
public class ImageCommandHandler implements CommandHandler {

    private final ImageSearchPresenter presenter;
    private final MessageSender sender;

    ImageCommandHandler(ImageSearchPresenter presenter, MessageSender sender) {
        this.presenter = presenter;
        this.sender = sender;
    }

    @Override
    public Set<String> commands() {
        return Set.of("/img", "/image");
    }

    @Override
    public String description() {
        return "Поиск картинок: /img кот";
    }

    @Override
    public void handle(CommandContext context) {
        if (!context.hasArguments()) {
            sender.sendText(context.chatId(), "Что искать? Пример: /img котики");
            return;
        }

        var query = context.arguments();
        var outcome = presenter.searchAt(query, 1);

        if (!(outcome instanceof ImageSearchOutcome.Found found)) {
            sender.sendText(context.chatId(), ImageSearchPresenter.describe(outcome));
            return;
        }

        var result = presenter.pick(found, 1);
        if (result == null) {
            sender.sendText(context.chatId(), "Ничего не нашёл по запросу: " + query);
            return;
        }

        var queryId = presenter.rememberQuery(query);
        sender.sendPhotoByUrl(
                context.chatId(),
                result.imageUrl(),
                presenter.caption(query, result, 1),
                presenter.keyboard(queryId, 1, found.page().hasMore()));
    }
}
