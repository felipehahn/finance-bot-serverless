package com.financebot.transactionservice.service;

import com.financebot.core.event.TelegramCommandEvent;

import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.command.CommandHandlerFactory;
import com.financebot.transactionservice.exception.BotException;
import com.financebot.transactionservice.session.UserSession;
import com.financebot.transactionservice.session.UserSessionRepository;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class FunctionService {

    @Inject
    private CommandHandlerFactory commandHandlerFactory;

    @Inject
    private UserSessionRepository userSessionRepository;

    @Inject
    private TelegramMessageService telegramMessageService;

    private static final Set<String> PRIORITY_COMMANDS = Set.of("/cancelar");

    public void process(TelegramCommandEvent event) {
        try {
            UserSession session = null;
            String command;

            if (event.command() != null && PRIORITY_COMMANDS.contains(event.command())) {
                command = event.command();
            } else {
                session = userSessionRepository.get(event.userId());
                command = session != null ? session.getCommand() : event.command();
            }

            CommandHandler handler = commandHandlerFactory.getHandler(command);
            handler.process(event, session);
        } catch (BotException e) {
            telegramMessageService.send(event.chatId(), "❌ " + e.getMessage());
        } catch (Exception e) {
            org.jboss.logging.Logger.getLogger(FunctionService.class).error("Command processing failed", e);
            telegramMessageService.send(event.chatId(), "❌ Ocorreu um erro inesperado. Tente novamente.");
        }
    }
}
