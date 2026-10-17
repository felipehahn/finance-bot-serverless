package com.financebot.core.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramChatDTO(
        Long id,
        @JsonProperty("first_name") String firstName,
        String type) {
}
