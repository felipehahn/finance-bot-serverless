package com.financebot.core.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelegramReplyKeyboardRemove(
        @JsonProperty("remove_keyboard") boolean removeKeyboard
) {
    public TelegramReplyKeyboardRemove() {
        this(true);
    }
}
