package com.financebot.transactionservice.utils;

import com.financebot.core.event.TelegramKeyboardButton;
import com.financebot.core.event.TelegramReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBuilder {

    public static TelegramReplyKeyboardMarkup build(List<String> options) {
        List<TelegramKeyboardButton> buttons = options.stream().map(TelegramKeyboardButton::new).toList();

        List<List<TelegramKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 2) {
            rows.add(buttons.subList(i, Math.min(i + 2, buttons.size())));
        }

        return new TelegramReplyKeyboardMarkup(rows, true, true);
    }
}
