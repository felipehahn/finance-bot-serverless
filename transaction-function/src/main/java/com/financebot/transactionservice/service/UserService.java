package com.financebot.transactionservice.service;

import com.financebot.transactionservice.model.User;
import com.financebot.transactionservice.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@jakarta.transaction.Transactional
public class UserService {

    @Inject
    UserRepository repository;

    public void createIfNotExists(Long telegramId) {
        repository.findByTelegramId(telegramId).orElseGet(() -> repository.save(User.create(telegramId)));
    }
}
