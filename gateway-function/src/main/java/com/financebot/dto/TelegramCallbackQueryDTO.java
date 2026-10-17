package com.financebot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramCallbackQueryDTO(
    String id,

    TelegramUserDTO from,

    TelegramMessageDTO message,

    @JsonProperty("chat_instance")
    String chatInstance,

    String data
) {}