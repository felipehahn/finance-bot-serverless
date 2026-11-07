package com.financebot.transactionservice.command.handler;

import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.dto.TransactionDTO;
import com.financebot.transactionservice.enums.ReportPeriod;
import com.financebot.transactionservice.enums.ReportType;
import com.financebot.transactionservice.enums.TransactionType;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.exception.BotException;
import com.financebot.transactionservice.service.TransactionService;
import com.financebot.transactionservice.session.UserSession;
import com.financebot.transactionservice.session.UserSessionRepository;
import com.financebot.transactionservice.session.UserSessionStep;
import com.financebot.transactionservice.utils.InlineKeyboardBuilder;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

@ApplicationScoped
public class ReportCommandHandler implements CommandHandler {
    private final Map<UserSessionStep, BiConsumer<TelegramCommandEvent, UserSession>> stepHandlers = Map.of(
            UserSessionStep.INITIAL_STEP, this::handleInit, UserSessionStep.WAITING_REPORT_TYPE, this::handleType,
            UserSessionStep.WAITING_REPORT_PERIOD, this::handlePeriod, UserSessionStep.WAITING_REPORT_DATE,
            this::handleDate);

    private static final NumberFormat CURRENCY_FORMAT;

    static {
        CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
    }

    @Inject
    private TransactionService transactionService;

    @Inject
    private UserSessionRepository userSessionRepository;

    @Inject
    private TelegramMessageService telegramMessageService;

    @Override
    public String getCommand() {
        return "/relatorio";
    }

    @Override
    public void process(TelegramCommandEvent event, UserSession session) {
        if (session == null)
            session = new UserSession(this.getCommand(), UserSessionStep.INITIAL_STEP);

        stepHandlers.get(session.getStep()).accept(event, session);
    }

    private void handleInit(TelegramCommandEvent event, UserSession session) {
        session.setStep(UserSessionStep.WAITING_REPORT_TYPE);
        userSessionRepository.save(event.userId(), session);

        telegramMessageService.send(event.chatId(), "Selecione o tipo do relatório:",
                InlineKeyboardBuilder
                        .build(List.of(List.of(InlineKeyboardBuilder.button("Despesas", ReportType.EXPENSE.toString()),
                                InlineKeyboardBuilder.button("Receitas", ReportType.INCOME.toString()),
                                InlineKeyboardBuilder.button("Ambas", ReportType.ALL.toString())))));
    }

    private void handleType(TelegramCommandEvent event, UserSession session) {
        ReportType reportType = ReportType.fromCallback(event.text());
        session.getData().put("type", reportType);
        session.setStep(UserSessionStep.WAITING_REPORT_PERIOD);
        userSessionRepository.save(event.userId(), session);

        telegramMessageService.send(event.chatId(), "Selecione o período:",
                InlineKeyboardBuilder.build(
                        List.of(List.of(InlineKeyboardBuilder.button("Mês atual", ReportPeriod.CURRENT.toString()),
                                InlineKeyboardBuilder.button("Outro período", ReportPeriod.CUSTOM.toString())))));
    }

    private void handlePeriod(TelegramCommandEvent event, UserSession session) {
        ReportPeriod period = ReportPeriod.fromCallback(event.text());

        if (period == ReportPeriod.CURRENT) {
            generateReport(event, session, YearMonth.now());
            return;
        }

        session.setStep(UserSessionStep.WAITING_REPORT_DATE);
        userSessionRepository.save(event.userId(), session);
        telegramMessageService.send(event.chatId(), "Informe o período no formato MM/yyyy:");
    }

    private void handleDate(TelegramCommandEvent event, UserSession session) {
        YearMonth yearMonth = parseYearMonth(event.text());
        generateReport(event, session, yearMonth);
    }

    private void generateReport(TelegramCommandEvent event, UserSession session, YearMonth yearMonth) {
        try {
            ReportType reportType = session.reportType();
            TransactionType transactionType = reportType == ReportType.INCOME ? TransactionType.INCOME
                    : reportType == ReportType.EXPENSE ? TransactionType.EXPENSE : null;

            List<TransactionDTO> transactions = transactionService.findByUserAndPeriod(event.userId(), yearMonth,
                    transactionType);

            if (transactions.isEmpty()) {
                telegramMessageService.send(event.chatId(), "Nenhuma transação encontrada para o período.");
                return;
            }

            telegramMessageService.send(event.chatId(), buildMessage(transactions, yearMonth, reportType));
        } finally {
            userSessionRepository.remove(event.userId());
        }
    }

    private String buildMessage(List<TransactionDTO> transactions, YearMonth yearMonth, ReportType reportType) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter periodFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        String titulo = switch (reportType) {
            case EXPENSE -> "Relatório de Despesas";
            case INCOME -> "Relatório de Receitas";
            case ALL -> "Relatório de Receitas e Despesas";
        };

        StringBuilder sb = new StringBuilder();

        sb.append("📊 *").append(titulo).append(" - ").append(yearMonth.format(periodFormatter)).append("*\n\n");

        for (TransactionDTO t : transactions) {
            String icon = t.type() == TransactionType.INCOME ? "💰 Receita" : "💸 Despesa";

            sb.append("📅 *").append(t.data().format(dateFormatter)).append("*");

            if (reportType == ReportType.ALL) {
                sb.append(" — ").append(icon);
            }

            sb.append("\n");
            sb.append("📝 Descrição: ").append(t.description()).append("\n");
            sb.append("💲 Valor: R$ ").append(CURRENCY_FORMAT.format(t.amount())).append("\n");
            sb.append("🔑 Identificador: ").append(t.id()).append("\n");
            sb.append("\n");
        }

        switch (reportType) {
            case EXPENSE -> {
                BigDecimal total = transactions.stream().map(TransactionDTO::amount).reduce(BigDecimal.ZERO,
                        BigDecimal::add);
                sb.append("💸 *Total de despesas: R$ ").append(CURRENCY_FORMAT.format(total)).append("*");
            }
            case INCOME -> {
                BigDecimal total = transactions.stream().map(TransactionDTO::amount).reduce(BigDecimal.ZERO,
                        BigDecimal::add);
                sb.append("💰 *Total de receitas: R$ ").append(CURRENCY_FORMAT.format(total)).append("*");
            }
            case ALL -> {
                BigDecimal total = transactions.stream()
                        .map(t -> t.type() == TransactionType.INCOME ? t.amount() : t.amount().negate())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                sb.append("📈 *Saldo: R$ ").append(CURRENCY_FORMAT.format(total)).append("*");
            }
        }

        return sb.toString();
    }

    private YearMonth parseYearMonth(String text) {
        try {
            return YearMonth.parse(text.trim(), DateTimeFormatter.ofPattern("MM/yyyy"));
        } catch (Exception e) {
            throw new BotException("Período inválido: " + text + ". Use o formato MM/yyyy.");
        }
    }
}
