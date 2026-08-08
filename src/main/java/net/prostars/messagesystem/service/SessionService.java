package net.prostars.messagesystem.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 현재 로그인 사용자와 HTTP Session을 관리하는 서비스.
 *
 * SecurityContext에서 로그인 사용자를 조회하고,
 * 저장된 Session의 TTL을 갱신한다.
 */
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
