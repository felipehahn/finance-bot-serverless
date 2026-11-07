package com.financebot.transactionservice.service;

import com.financebot.transactionservice.dto.TransactionDTO;
import com.financebot.transactionservice.enums.TransactionType;
import com.financebot.transactionservice.exception.BotException;
import com.financebot.transactionservice.model.Transaction;
import com.financebot.transactionservice.model.User;
import com.financebot.transactionservice.repository.TransactionRepository;
import com.financebot.transactionservice.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@jakarta.transaction.Transactional
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    UserRepository userRepository;

    public Transaction create(Long telegramUserId, TransactionDTO transactionDTO) {
        User user = userRepository.findByTelegramId(telegramUserId).orElseThrow();
        return transactionRepository.save(Transaction.create(user, transactionDTO));
    }

    public void delete(Long id, Long userId) {
        if (!transactionRepository.existsByIdAndUserTelegramId(id, userId))
            throw new BotException("Transação não encontrada.");

        transactionRepository.deleteById(id);
    }

    public List<TransactionDTO> findByUserAndPeriod(Long userId, YearMonth yearMonth, TransactionType type) {
        return transactionRepository.listTransactionsPeriod(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth(), type);
    }
}
