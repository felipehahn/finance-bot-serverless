package com.financebot.transactionservice.command.handler;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.session.UserSession;
import com.financebot.transactionservice.session.UserSessionRepository;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CancelCommandHandler implements CommandHandler {

    @Inject
    private UserSessionRepository userSessionRepository;

    @Inject
    private TelegramMessageService telegramMessageService;

    @Override
    public String getCommand() {
        return "/cancelar";
    }

    @Override
    public void process(TelegramCommandEvent event, UserSession session) {
        userSessionRepository.remove(event.userId());
        telegramMessageService.send(event.chatId(), "Ação cancelada com sucesso.");
    }
}
