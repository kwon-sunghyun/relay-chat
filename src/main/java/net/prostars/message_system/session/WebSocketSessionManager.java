package net.prostars.message_system.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Session을 관리하는 클래스.
 * Session 등록, 조회, 종료 기능을 제공한다.
 */
@Component
public class WebSocketSessionManager {


    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);
    // 여러 Client가 동시에 접근하므로 ConcurrentHashMap 사용
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 현재 연결된 Session 목록 반환 (브로드캐스트 순회를 위해 List로 변환)
    public List<WebSocketSession> getSeesions() {
        return sessions.values().stream().toList();
    }

    public void storeSession(WebSocketSession webSocketSession) {
        log.info("Store Session : {}", webSocketSession.getId());
        // 신규 WebSocket Session 저장
        sessions.put(webSocketSession.getId(), webSocketSession);
    }

    // Session 제거 후 연결 종료
    public void terminateSession(String sessionId) {
        try {
            WebSocketSession webSocketSession = sessions.remove(sessionId);
            if (webSocketSession != null) {
                log.info("Remove session: {}", sessionId);
                webSocketSession.close();
            log.info("Close session: {}", sessionId);
            }
        } catch (Exception ex) {
            log.error("Failed WebSocketSession close. sessionId: {}", sessionId);
        }
    }
}
