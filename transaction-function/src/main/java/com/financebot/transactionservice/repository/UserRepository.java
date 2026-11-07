package com.financebot.transactionservice.repository;

import com.financebot.transactionservice.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public Optional<User> findByTelegramId(Long telegramId) {
        return find("telegramId", telegramId).firstResultOptional();
    }

    public User save(User user) {
        persist(user);
        return user;
    }
}
