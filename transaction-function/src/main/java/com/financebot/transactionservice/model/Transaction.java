package com.financebot.transactionservice.model;

import com.financebot.transactionservice.dto.TransactionDTO;
import com.financebot.transactionservice.enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Transaction create(User user, TransactionDTO transactionDTO) {
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(transactionDTO.type());
        transaction.setDescription(transactionDTO.description());
        transaction.setAmount(transactionDTO.amount());
        transaction.setData(transactionDTO.data());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        return transaction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        this.id = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User value) {
        this.user = value;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType value) {
        this.type = value;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate value) {
        this.data = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime value) {
        this.createdAt = value;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime value) {
        this.updatedAt = value;
    }

    public Transaction() {
    }
}
