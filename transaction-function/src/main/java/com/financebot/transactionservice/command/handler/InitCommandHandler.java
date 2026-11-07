package com.financebot.transactionservice.command.handler;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.service.UserService;
import com.financebot.transactionservice.session.UserSession;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InitCommandHandler implements CommandHandler {

    @Inject
    private UserService userService;

    @Inject
    private TelegramMessageService telegramMessageService;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void process(TelegramCommandEvent event, UserSession session) {
        userService.createIfNotExists(event.userId());

        String text = "Olá, " + event.username() + "! 👋\n\n"
                + "Bem-vindo ao *Finance Bot*, seu assistente de finanças pessoais.\n\n"
                + "Com ele você pode registrar despesas e receitas, acompanhar seu saldo "
                + "e gerar relatórios diretamente pelo Telegram.\n\n"
                + "Use /ajuda para ver todos os comandos disponíveis.";
        telegramMessageService.send(event.chatId(), text);
    }
}
