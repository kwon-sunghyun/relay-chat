package net.prostars.messagesystem.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SessionService {

    private final SessionRepository<? extends Session> httpSessionRepository;

    public SessionService(SessionRepository<? extends Session> httpSessionRepository) {
        this.httpSessionRepository = httpSessionRepository;
    }

    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public void refreshTTL(String httpsessionId) {
        Session httpSession = httpSessionRepository.findById(httpsessionId);
        if (httpSession != null) {
            httpSession.setLastAccessedTime(Instant.now());

        }
    }
}
