package com.financebot.transactionservice.command.handler;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.session.UserSession;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HelpCommandHandler implements CommandHandler {

    @Inject
    private TelegramMessageService telegramMessageService;

    @Override
    public String getCommand() {
        return "/ajuda";
    }

    @Override
    public void process(TelegramCommandEvent event, UserSession session) {
        String text = "Aqui estão os comandos disponíveis:\n\n" + "/despesa — registrar uma despesa\n"
                + "/receita — registrar uma receita\n" + "/relatorio — gerar relatório por período\n"
                + "/deltransacao — excluir uma transação\n" + "/cancelar — cancelar a operação atual\n\n"
                + "Em qualquer etapa, use /cancelar para voltar ao início.";
        telegramMessageService.send(event.chatId(), text);
    }
}
