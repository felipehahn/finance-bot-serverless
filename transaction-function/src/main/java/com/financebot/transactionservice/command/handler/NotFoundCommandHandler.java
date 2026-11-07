package com.financebot.transactionservice.command.handler;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.session.UserSession;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotFoundCommandHandler implements CommandHandler {

    @Inject
    private TelegramMessageService telegramMessageService;

    @Override
    public String getCommand() {
        return "/default";
    }

    @Override
    public void process(TelegramCommandEvent event, UserSession session) {
        telegramMessageService.send(event.chatId(),
                "Comando não reconhecido. Use /ajuda para ver os comandos disponíveis.");
    }
}
