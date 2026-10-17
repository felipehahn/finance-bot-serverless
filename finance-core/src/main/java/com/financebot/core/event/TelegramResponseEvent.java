package com.financebot.core.event;

public record TelegramResponseEvent(Long chatId, String text, Object replyMarkup) {}
