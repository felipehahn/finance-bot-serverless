package com.financebot.transactionservice.command;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.session.UserSession;

public interface CommandHandler {
    String getCommand();

    void process(TelegramCommandEvent event, UserSession session);
}
