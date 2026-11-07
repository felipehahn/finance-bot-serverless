package com.financebot.transactionservice.service;

import com.financebot.transactionservice.dto.TransactionDTO;
import com.financebot.transactionservice.enums.TransactionType;
import com.financebot.transactionservice.exception.BotException;
import com.financebot.transactionservice.model.*;
import com.financebot.transactionservice.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    TransactionService service;

    @Test
    void creationUsesRegisteredUserAndPreservesTransactionFields() {
        User user = User.create(20L);
        when(userRepository.findByTelegramId(20L)).thenReturn(Optional.of(user));
        when(transactionRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        TransactionDTO dto = new TransactionDTO(null, new BigDecimal("42.50"), TransactionType.EXPENSE, "Lunch",
                LocalDate.of(2026, 10, 25));
        Transaction result = service.create(20L, dto);
        assertSame(user, result.getUser());
        assertEquals(dto.amount(), result.getAmount());
        assertEquals(dto.type(), result.getType());
        assertEquals(dto.description(), result.getDescription());
        assertEquals(dto.data(), result.getData());
        assertNotNull(result.getCreatedAt());
        assertEquals(result.getCreatedAt(), result.getUpdatedAt());
    }

    @Test
    void deletionRejectsTransactionNotOwnedByUser() {
        when(transactionRepository.existsByIdAndUserTelegramId(7L, 20L)).thenReturn(false);
        assertThrows(BotException.class, () -> service.delete(7L, 20L));
        verify(transactionRepository, never()).deleteById(anyLong());
    }

    @Test
    void deletionRemovesOwnedTransaction() {
        when(transactionRepository.existsByIdAndUserTelegramId(7L, 20L)).thenReturn(true);
        service.delete(7L, 20L);
        verify(transactionRepository).deleteById(7L);
    }

    @Test
    void reportIncludesWholeLeapYearMonthAndRequestedType() {
        service.findByUserAndPeriod(20L, YearMonth.of(2024, 2), TransactionType.INCOME);
        verify(transactionRepository).listTransactionsPeriod(20L, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29),
                TransactionType.INCOME);
    }
}
