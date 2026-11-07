package com.financebot.transactionservice.repository;

import com.financebot.transactionservice.dto.TransactionDTO;
import com.financebot.transactionservice.enums.TransactionType;
import com.financebot.transactionservice.model.Transaction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {
    public Transaction save(Transaction transaction) {
        persist(transaction);
        return transaction;
    }

    public boolean existsByIdAndUserTelegramId(Long id, Long telegramId) {
        return count("id = ?1 and user.telegramId = ?2", id, telegramId) > 0;
    }

    public List<TransactionDTO> listTransactionsPeriod(Long userId, LocalDate startDate, LocalDate endDate,
            TransactionType type) {
        List<Transaction> transactions = type == null
                ? list("user.telegramId = ?1 and data >= ?2 and data <= ?3 order by data asc", userId, startDate,
                        endDate)
                : list("user.telegramId = ?1 and data >= ?2 and data <= ?3 and type = ?4 order by data asc", userId,
                        startDate, endDate, type);
        return transactions.stream()
                .map(t -> new TransactionDTO(t.getId(), t.getAmount(), t.getType(), t.getDescription(), t.getData()))
                .toList();
    }
}
