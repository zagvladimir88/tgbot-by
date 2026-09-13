package ru.zagvladimir.tgbot.telegram.internal.handler;

import org.springframework.stereotype.Component;
import ru.zagvladimir.tgbot.image.ImageSearchOutcome;
import ru.zagvladimir.tgbot.telegram.MessageSender;
import ru.zagvladimir.tgbot.telegram.internal.BotRequest;
import ru.zagvladimir.tgbot.telegram.internal.CallbackHandler;

@Component
class ImageCallbackHandler implements CallbackHandler {

    private final ImageSearchPresenter presenter;
    private final MessageSender sender;

    ImageCallbackHandler(ImageSearchPresenter presenter, MessageSender sender) {
        this.presenter = presenter;
        this.sender = sender;
    }

    @Override
    public String prefix() {
        return ImageSearchPresenter.CALLBACK_PREFIX;
    }

    @Override
    public void handle(BotRequest.Callback callback) {
        var parts = ImageSearchPresenter.parseCallback(callback.data());
        if (parts.size() != 2) {
            sender.answerCallback(callback.callbackId(), "Не понял кнопку");
            return;
        }

        var query = presenter.queryById(parts.getFirst());
        if (query == null) {
            sender.answerCallback(callback.callbackId(), "Этот поиск уже устарел, повторите /img");
            return;
        }

        int index;
        try {
            index = Integer.parseInt(parts.get(1));
        } catch (NumberFormatException e) {
            sender.answerCallback(callback.callbackId(), "Не понял кнопку");
            return;
        }

        var outcome = presenter.searchAt(query, index);
        if (!(outcome instanceof ImageSearchOutcome.Found found)) {
            sender.answerCallback(callback.callbackId(), ImageSearchPresenter.describe(outcome));
            return;
        }

        var result = presenter.pick(found, index);
        if (result == null) {
            sender.answerCallback(callback.callbackId(), "Дальше ничего нет");
            return;
        }

        sender.answerCallback(callback.callbackId(), "");
        sender.editPhotoByUrl(
                callback.chatId(),
                callback.messageId(),
                result.imageUrl(),
                presenter.caption(query, result, index),
                presenter.keyboard(parts.getFirst(), index, found.page().hasMore()));
    }
}
