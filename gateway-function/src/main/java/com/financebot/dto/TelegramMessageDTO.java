package com.financebot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessageDTO (
    @JsonProperty("message_id")
    Long messageId,

    TelegramUserDTO from,

    TelegramChatDTO chat,

    Long date,

    String text
) {}
