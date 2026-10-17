package com.financebot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUserDTO (
    Long id,

    @JsonProperty("first_name")
    String firstName,

    @JsonProperty("is_bot")
    Boolean isBot,

    @JsonProperty("language_code")
    String languageCode
) {}