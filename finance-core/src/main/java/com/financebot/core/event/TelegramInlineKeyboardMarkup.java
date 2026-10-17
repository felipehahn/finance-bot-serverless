package com.financebot.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TelegramInlineKeyboardMarkup(
        @JsonProperty("inline_keyboard") List<List<TelegramInlineKeyboardButton>> inlineKeyboard
) {}