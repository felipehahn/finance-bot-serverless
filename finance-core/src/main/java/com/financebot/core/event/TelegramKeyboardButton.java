package com.financebot.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramKeyboardButton(
        @JsonProperty("text") String text
) {}
