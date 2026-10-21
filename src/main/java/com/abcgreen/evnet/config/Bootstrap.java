package com.abcgreen.evnet.config;

import com.abcgreen.evnet.service.AuthService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Bootstrap {
    private final AuthService auth;
    public Bootstrap(AuthService auth) { this.auth = auth; }

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        auth.bootstrapAdmin();
    }
}
