package com.github.ar4ik4ik.cloudstorage.listener;

import com.github.ar4ik4ik.cloudstorage.event.UserRegisteredEvent;
import com.github.ar4ik4ik.cloudstorage.service.AuthService;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegisterEventListener {

    private final StorageService storageService;
    private final AuthService authService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredEvent(@NotNull UserRegisteredEvent event) {
        storageService.createRootDirectoryForUser(event.getUserId());
    }
}
