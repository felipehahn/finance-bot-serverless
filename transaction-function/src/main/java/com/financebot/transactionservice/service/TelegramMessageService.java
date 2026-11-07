package com.financebot.transactionservice.service;

import com.financebot.core.client.TelegramClient;
import jakarta.enterprise.context.ApplicationScoped;

/** Sends command replies directly to Telegram. */
@ApplicationScoped
public class TelegramMessageService {
    private final TelegramClient client;

    public TelegramMessageService(TelegramClient client) {
        this.client = client;
    }

    public void send(Long chatId, String text) {
        send(chatId, text, null);
    }

    public void send(Long chatId, String text, Object replyMarkup) {
        client.sendMessage(chatId, text, replyMarkup);
    }
}
