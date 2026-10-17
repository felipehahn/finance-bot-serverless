package com.financebot.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TelegramReplyKeyboardMarkup(
        @JsonProperty("keyboard") List<List<TelegramKeyboardButton>> keyboard,
        @JsonProperty("resize_keyboard") boolean resizeKeyboard,
        @JsonProperty("one_time_keyboard") boolean oneTimeKeyboard
) {}
