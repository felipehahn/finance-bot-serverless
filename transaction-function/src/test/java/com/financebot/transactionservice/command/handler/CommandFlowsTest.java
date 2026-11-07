package com.financebot.transactionservice.command.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebot.core.client.TelegramClient;
import com.financebot.core.event.*;
import com.financebot.transactionservice.TransactionFunction;
import com.financebot.transactionservice.dto.TransactionDTO;
import com.financebot.transactionservice.enums.*;
import com.financebot.transactionservice.service.FunctionService;
import com.financebot.transactionservice.command.CommandHandler;
import com.financebot.transactionservice.command.CommandHandlerFactory;
import com.financebot.transactionservice.service.TelegramMessageService;
import com.financebot.transactionservice.service.*;
import com.financebot.transactionservice.session.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CommandFlowsTest {
    private final TelegramClient telegram = mock(TelegramClient.class);
    private final UserSessionRepository sessions = mock(UserSessionRepository.class);
    private final TransactionService transactions = mock(TransactionService.class);
    private final UserService users = mock(UserService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final TelegramMessageService telegramMessageService = new TelegramMessageService(telegram);
    private FunctionService functionService;
    private TransactionFunction function;

    @BeforeEach
    void setup() throws Exception {
        List<CommandHandler> handlers = List.of(new InitCommandHandler(), new HelpCommandHandler(),
                new IncomeCommandHandler(), new ExpenseCommandHandler(), new ReportCommandHandler(),
                new DeleteTransactionCommandHandler(), new CancelCommandHandler(), new NotFoundCommandHandler());
        for (CommandHandler handler : handlers) {
            inject(handler, "telegramMessageService", telegramMessageService);
            inject(handler, "userSessionRepository", sessions);
            inject(handler, "transactionService", transactions);
            inject(handler, "userService", users);
        }
        @SuppressWarnings("unchecked")
        jakarta.enterprise.inject.Instance<CommandHandler> instances = mock(jakarta.enterprise.inject.Instance.class);
        when(instances.stream()).thenReturn(handlers.stream());
        CommandHandlerFactory factory = new CommandHandlerFactory(instances);
        functionService = new FunctionService();
        inject(functionService, "telegramMessageService", telegramMessageService);
        inject(functionService, "userSessionRepository", sessions);
        inject(functionService, "commandHandlerFactory", factory);
        function = new TransactionFunction(functionService);
        // Redis returns a fresh JSON-decoded session on each Lambda invocation.
        doAnswer(call -> {
            UserSession saved = mapper.readValue(mapper.writeValueAsString(call.getArgument(1)), UserSession.class);
            when(sessions.get(20L)).thenReturn(saved);
            return null;
        }).when(sessions).save(eq(20L), any());
        doAnswer(call -> {
            when(sessions.get(20L)).thenReturn(null);
            return null;
        }).when(sessions).remove(20L);
    }

    @Test
    void startRegistersUserAndWelcomesThem() {
        send("/start", null);
        verify(users).createIfNotExists(20L);
        verify(telegram).sendMessage(eq(10L), contains("Alice"), isNull());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/receita", "/despesa" })
    void recordsTransactionAcrossSeparateInvocations(String command) {
        send(command, null);
        send(null, "123,45");
        send(null, "Example");
        send(null, "25/10/2026");
        verify(transactions).create(eq(20L),
                eq(new TransactionDTO(null, new BigDecimal("123.45"),
                        command.equals("/receita") ? TransactionType.INCOME : TransactionType.EXPENSE, "Example",
                        LocalDate.of(2026, 10, 25))));
        verify(sessions).remove(20L);
        verify(telegram).sendMessage(eq(10L), contains("registrada"), any(TelegramReplyKeyboardRemove.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/receita", "/despesa" })
    void invalidAmountKeepsFlowAvailableForRetry(String command) {
        send(command, null);
        send(null, "invalid");
        verify(telegram).sendMessage(eq(10L), contains("Valor inválido"), isNull());
        verifyNoInteractions(transactions);
        assertEquals(UserSessionStep.WAITING_AMOUNT, sessions.get(20L).getStep());
    }

    @ParameterizedTest
    @ValueSource(strings = { "INCOME", "EXPENSE", "ALL" })
    void customReportFiltersAndCalculatesTotals(String type) {
        TransactionType filter = type.equals("ALL") ? null : TransactionType.valueOf(type);
        when(transactions.findByUserAndPeriod(20L, YearMonth.of(2026, 9), filter))
                .thenReturn(List.of(new TransactionDTO(7L, new BigDecimal("12.50"),
                        filter == null ? TransactionType.INCOME : filter, "Example", LocalDate.of(2026, 9, 1))));
        send("/relatorio", null);
        send(null, type);
        send(null, "CUSTOM");
        send(null, "09/2026");
        verify(transactions).findByUserAndPeriod(20L, YearMonth.of(2026, 9), filter);
        verify(telegram).sendMessage(eq(10L), contains("12,50"), isNull());
        verify(sessions).remove(20L);
    }

    @Test
    void currentMonthReportHandlesEmptyResults() {
        when(transactions.findByUserAndPeriod(20L, YearMonth.now(), null)).thenReturn(List.of());
        send("/relatorio", null);
        send(null, "ALL");
        send(null, "CURRENT");
        verify(telegram).sendMessage(eq(10L), contains("Nenhuma transação"), isNull());
        verify(sessions).remove(20L);
    }

    @Test
    void deletesTransactionForCurrentUser() {
        send("/deltransacao", null);
        send(null, "7");
        verify(transactions).delete(7L, 20L);
        verify(sessions).remove(20L);
    }

    @Test
    void invalidDeletionClearsSessionAndExplainsError() {
        send("/deltransacao", null);
        send(null, "abc");
        verifyNoInteractions(transactions);
        verify(sessions).remove(20L);
        verify(telegram).sendMessage(eq(10L), contains("Código inválido"), isNull());
    }

    @Test
    void cancelOverridesActiveSession() {
        send("/receita", null);
        send("/cancelar", null);
        verify(sessions).remove(20L);
        verify(telegram).sendMessage(eq(10L), contains("cancelada"), isNull());
        verifyNoInteractions(transactions);
    }

    @Test
    void helpAndUnknownCommandsReplyDirectlyToTelegram() {
        send("/ajuda", null);
        send("/unknown", null);
        send(null, "hello");
        verify(telegram).sendMessage(eq(10L), contains("/despesa"), isNull());
        verify(telegram, times(2)).sendMessage(eq(10L), contains("Comando não reconhecido"), isNull());
    }

    @Test
    void unexpectedFailureReturnsFriendlyError() {
        doThrow(new IllegalStateException("database unavailable")).when(users).createIfNotExists(20L);
        send("/start", null);
        verify(telegram).sendMessage(eq(10L), contains("erro inesperado"), isNull());
    }

    private void send(String command, String text) {
        assertNull(function.handleRequest(new TelegramCommandEvent(10L, 20L, "Alice", command, text, 1L), null));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException ignored) {
            // Each handler only declares the collaborators its command uses.
        }
    }
}
