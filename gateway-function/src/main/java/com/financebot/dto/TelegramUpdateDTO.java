package com.financebot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUpdateDTO (
    @JsonProperty("update_id")
    Long updateId,

    TelegramMessageDTO message,

    @JsonProperty("callback_query")
    TelegramCallbackQueryDTO callbackQuery
) {}
