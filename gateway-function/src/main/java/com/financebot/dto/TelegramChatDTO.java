package com.financebot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramChatDTO (Long id,  @JsonProperty("first_name") String firstName, String type){}