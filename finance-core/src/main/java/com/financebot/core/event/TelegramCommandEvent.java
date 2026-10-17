package com.financebot.core.event;

public record TelegramCommandEvent(
        Long chatId,
        Long userId,
        String username,
        String command,
        String text,
        Long messageId
) {
}
