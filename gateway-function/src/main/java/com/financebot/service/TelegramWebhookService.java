package com.financebot.service;

import com.financebot.dto.TelegramUpdateDTO;
import com.financebot.idempotency.IdempotencyService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TelegramWebhookService {

    private final ProcessorInvoker processorInvoker;
    private final TelegramClient telegramClient;
    private final IdempotencyService idempotencyService;

    public TelegramWebhookService(ProcessorInvoker processorInvoker,
                                  TelegramClient telegramClient,
                                  IdempotencyService idempotencyService) {
        this.processorInvoker = processorInvoker;
        this.telegramClient = telegramClient;
        this.idempotencyService = idempotencyService;
    }

    public void process(TelegramUpdateDTO update) {
        if (!idempotencyService.isNew(update.updateId().toString()))
            return;

        if (update.message() != null && update.message().text() != null) {
            handleMessage(update);
            return;
        }

        if (update.callbackQuery() != null)
            handleCallback(update);
    }

    private void handleMessage(TelegramUpdateDTO update) {
        TelegramMessageDTO message = update.message();
        String text = message.text();

        String[] messageTextParts = text.startsWith("/") ?
                text.split(" ", 2)
                : new String[]{null, text};

        String command = messageTextParts[0];
        text = messageTextParts.length > 1 ? messageTextParts[1] : null;

        TelegramCommandEvent event = new TelegramCommandEvent(
                message.chat().id(),
                message.from().id(),
                message.from().firstName(),
                command,
                text,
                message.messageId()
        );

        publisher.publishEvent(event);
    }

    private void handleCallback(TelegramUpdateDTO update) {
        TelegramCallbackQueryDTO callback = update.callbackQuery();

        TelegramCommandEvent event = new TelegramCommandEvent(
                callback.message().chat().id(),
                callback.from().id(),
                callback.from().firstName(),
                null,
                callback.data(),
                callback.message().messageId()
        );

        publisher.publishEvent(event);
        telegramClient.answerCallback(callback.id());
    }
}
