package com.financebot.transactionservice.exception;

public class BotException extends RuntimeException {
    public BotException(String message) {
        super(message);
    }
}
