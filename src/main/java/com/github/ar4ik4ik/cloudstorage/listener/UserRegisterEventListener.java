package com.github.ar4ik4ik.cloudstorage.listener;

import com.github.ar4ik4ik.cloudstorage.event.UserRegisteredEvent;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import com.github.ar4ik4ik.cloudstorage.service.impl.LoginAuthService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegisterEventListener {

    private final StorageService storageService;
    private final LoginAuthService authService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredEvent(@NotNull UserRegisteredEvent event) {
        storageService.createRootDirectoryForUser(event.getUserId());
    }
}
