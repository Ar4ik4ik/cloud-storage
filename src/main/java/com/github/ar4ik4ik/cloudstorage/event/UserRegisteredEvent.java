package com.github.ar4ik4ik.cloudstorage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;



@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final Integer userId;

    public UserRegisteredEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }
}
