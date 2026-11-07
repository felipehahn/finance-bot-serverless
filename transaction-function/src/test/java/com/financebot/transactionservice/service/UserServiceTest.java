package com.financebot.transactionservice.service;

import com.financebot.transactionservice.model.User;
import com.financebot.transactionservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository repository;
    @InjectMocks
    UserService service;

    @Test
    void registersNewTelegramUser() {
        when(repository.findByTelegramId(20L)).thenReturn(Optional.empty());
        service.createIfNotExists(20L);
        var user = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(repository).save(user.capture());
        assertEquals(20L, user.getValue().getTelegramId());
        assertNotNull(user.getValue().getCreatedAt());
    }

    @Test
    void repeatedStartDoesNotCreateDuplicateUser() {
        when(repository.findByTelegramId(20L)).thenReturn(Optional.of(User.create(20L)));
        service.createIfNotExists(20L);
        verify(repository, never()).save(any());
    }
}
