package com.financebot.transactionservice.command.handler;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.exception.BotException;
import com.financebot.transactionservice.service.TransactionService;
import com.financebot.transactionservice.session.UserSession;
import com.financebot.transactionservice.session.UserSessionRepository;
import com.financebot.transactionservice.session.UserSessionStep;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.function.BiConsumer;

@ApplicationScoped
public class DeleteTransactionCommandHandler implements CommandHandler {

    private final Map<UserSessionStep, BiConsumer<TelegramCommandEvent, UserSession>> stepHandlers = Map
            .of(UserSessionStep.INITIAL_STEP, this::handleInit, UserSessionStep.WAITING_ID, this::handleId);

    @Inject
    private TransactionService transactionService;

    @Inject
    private UserSessionRepository userSessionRepository;

    @Inject
    private TelegramMessageService telegramMessageService;

    @Override
    public String getCommand() {
        return "/deltransacao";
    }

    @Override
    public void process(TelegramCommandEvent event, UserSession session) {
        if (session == null)
            session = new UserSession(this.getCommand(), UserSessionStep.INITIAL_STEP);

        stepHandlers.get(session.getStep()).accept(event, session);
    }

    private void handleInit(TelegramCommandEvent event, UserSession session) {
        session.setStep(UserSessionStep.WAITING_ID);
        userSessionRepository.save(event.userId(), session);
        telegramMessageService.send(event.chatId(), "Informe o identificador da transação:");
    }

    private void handleId(TelegramCommandEvent event, UserSession session) {
        try {
            Long id = parseId(event.text());
            transactionService.delete(id, event.userId());
            telegramMessageService.send(event.chatId(), "✅ Transação excluída com sucesso!");
        } finally {
            userSessionRepository.remove(event.userId());
        }
    }

    private Long parseId(String idString) {
        try {
            return Long.parseLong(idString.trim());
        } catch (Exception e) {
            throw new BotException("Código inválido: " + idString + ". Informe apenas números inteiros.");
        }
    }
}
