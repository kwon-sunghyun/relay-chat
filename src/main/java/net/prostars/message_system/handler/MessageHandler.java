package net.prostars.message_system.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.prostars.message_system.dto.Message;
import net.prostars.message_system.session.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 메시지를 처리하는 Handler.
 * - Client 연결/종료 관리
 * - 그룹 채팅 메시지 브로드캐스트
 * - 전송 오류 처리
 */
@Component
public class MessageHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    // JSON ↔ Java 객체 변환
    private final ObjectMapper objectMapper = new ObjectMapper();
    // WebSocket Session 저장 및 관리
    private final WebSocketSessionManager webSocketSessionManager;

    public MessageHandler(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("ConnectionEstablished : {}", session.getId());

        // Thread-Safe한 메시지 전송을 위해 Decorator 적용
        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator =
                new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);
        // Session 등록
        webSocketSessionManager.storeSession(concurrentWebSocketSessionDecorator);

    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("TransportError: [{}] from {}", exception.getMessage(), session.getId());
        // 전송 오류 발생 시 Session 제거
        webSocketSessionManager.terminateSession(session.getId());
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        log.info("ConnectionClosed: [{}] from {}", status, session.getId());
        // 연결 종료 시 Session 제거
        webSocketSessionManager.terminateSession(session.getId());
    }


    @Override
    protected void handleTextMessage(WebSocketSession senderSession, @NonNull TextMessage message) throws Exception {
        log.info("Received TextMessage: [{}] from {}", message, senderSession.getId());
        String payload = message.getPayload();
        try {
            // JSON → Message 객체 변환
            Message receivedMessage = objectMapper.readValue(payload, Message.class);
            // 발신자를 제외한 모든 참여자에게 메시지 전달
            webSocketSessionManager.getSeesions().forEach(participantSession -> {
                if (!senderSession.getId().equals(participantSession.getId())) {
                    sendMessage(participantSession, receivedMessage);
                }
            });

        } catch (Exception ex) {
            String errorMessage = "유효한 프로토콜이 아닙니다.";
            log.error("errorMessage payload: {} from {}", payload, senderSession.getId());
            // 잘못된 JSON 요청 시 에러 메시지 반환
            sendMessage(senderSession, new Message("system", errorMessage));
        }
    }


    private void sendMessage(WebSocketSession session, Message message) {
        try {
            String msg = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(msg));
            log.info("send message: {} to {}", msg, session.getId());
        } catch (Exception ex) {
            log.error("메시지 전송 실패 to {} error: {}", session.getId(), ex.getMessage());
        }
    }
}
