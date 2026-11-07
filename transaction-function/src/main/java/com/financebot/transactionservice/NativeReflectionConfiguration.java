package com.financebot.transactionservice;

import com.financebot.core.event.*;
import com.financebot.transactionservice.session.UserSession;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = { UserSession.class, TelegramCommandEvent.class, TelegramInlineKeyboardMarkup.class,
        TelegramInlineKeyboardButton.class, TelegramReplyKeyboardMarkup.class, TelegramKeyboardButton.class,
        TelegramReplyKeyboardRemove.class })
public class NativeReflectionConfiguration {
}
