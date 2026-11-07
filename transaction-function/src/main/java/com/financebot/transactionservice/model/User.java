package com.financebot.transactionservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User(Long id) {
        this.id = id;
    }

    public static User create(Long telegramId) {
        User user = new User();
        user.setTelegramId(telegramId);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        this.id = value;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long value) {
        this.telegramId = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }

    public User() {
    }
}
