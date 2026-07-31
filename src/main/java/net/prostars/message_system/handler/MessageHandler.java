package net.prostars.message_system.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.prostars.message_system.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 메시지를 처리하는 Handler.
 *
 * <p>
 * 두 개의 WebSocket Session을 관리하며,
 * 한 사용자가 보낸 메시지를 상대방에게 전달하는
 * 1:1 채팅 기능을 제공한다.
 * </p>
 *
 * <p>
 * 처리 흐름
 *
 * 연결 요청 → afterConnectionEstablished() → leftSide / rightSide 저장 → handleTextMessage()
 * → 상대 Session으로 메시지 전달 → afterConnectionClosed() → Session 제거
 * </p>
 */
@Component
public class MessageHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    /**
     * JSON ↔ Java 객체 변환을 위한 ObjectMapper.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 첫 번째 사용자 Session.
     */
    private WebSocketSession leftSide = null;
    /**
     * 두 번째 사용자 Session.
     */
    private WebSocketSession rightSide = null;

    /**
     * WebSocket 연결이 성공하면 호출된다.
     *
     * <p>
     * 최초 접속자는 leftSide,
     * 두 번째 접속자는 rightSide에 저장한다.
     *
     * 두 명이 이미 접속 중이라면
     * 새로운 연결은 거부한다.
     * </p>
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("ConnectionEstablished : {}", session.getId());

        // 첫 번째 접속자 저장
        if (leftSide == null) {
            leftSide = session;
            return;
            // 두 번째 접속자 저장
        } else if (rightSide == null) {
            rightSide = session;
            return;
        }
        log.warn("빈 자리 없음. {}의 접속 거부.", session.getId());
        // 두 자리 모두 사용 중인 경우
        session.close();

    }

    /**
     * WebSocket 통신 중 예외가 발생하면 호출된다.
     *
     * 예를 들어 네트워크 단절, 강제 종료 등의 상황에서
     * 오류 로그를 기록한다.
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("TransportError: [{}] from {}", exception.getMessage(), session.getId());
    }

    /**
     * WebSocket 연결이 종료되면 호출된다.
     *
     * 종료된 Session을 제거하여
     * 이후 새로운 사용자가 접속할 수 있도록 한다.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("ConnectionClosed: [{}] from {}", status, session.getId());
        if (leftSide == session) {
            leftSide = null;
        } else if (rightSide == session) {
            rightSide = null;
        }
    }

    /**
     * 클라이언트가 TextMessage를 전송하면 호출된다.
     *
     * <p>
     * JSON 데이터를 Message 객체로 변환한 뒤,
     * 현재 송신자의 반대편 Session으로 메시지를 전달한다.
     *
     * JSON 형식이 올바르지 않으면
     * 오류 메시지를 송신자에게 반환한다.
     * </p>
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received TextMessage: [{}] from {}", message, session.getId());
        String payload = message.getPayload();
        try {
            // JSON 문자열을 Message 객체로 변환
            Message receivedMessage = objectMapper.readValue(payload, Message.class);
            // 현재 송신자가 left라면 right에게 전달
            if (leftSide == session) {
                sendMessage(rightSide, receivedMessage.content());
                // 현재 송신자가 right라면 left에게 전달
            } else if (rightSide == session) {
                sendMessage(leftSide, receivedMessage.content());
            }

        // JSON 파싱 실패
        } catch (Exception ex) {
            String errorMessage = "유효한 프로토콜이 아닙니다.";
            log.error("errorMessage payload: {} from {}", payload, session.getId());
            sendMessage(session,errorMessage);
        }
    }

    /**
     * 지정한 Session으로 메시지를 전송한다.
     *
     * @param session 메시지를 받을 WebSocket Session
     * @param message 전송할 메시지
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
//            String msg = objectMapper.writeValueAsString(new Message(message));
            session.sendMessage(new TextMessage(message));
            log.info("send message: {} to {}", message, session.getId());
        } catch (Exception ex) {
            log.error("메시지 전송 실패 to {} error: {}", session.getId(), ex.getMessage());
        }
    }
}
