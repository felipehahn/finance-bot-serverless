package com.financebot.core.dto.telegram_client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public record SendMessageRequest(
        @JsonProperty("chat_id") Long chatId,
        @JsonProperty("text") String text,
        @JsonProperty("reply_markup") Object replyMarkup) {

    public SendMessageRequest(Long chatId, String text) {
        this(chatId, text, null);
    }
}
