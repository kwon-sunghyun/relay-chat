package net.prostars.messagesystem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.prostars.messagesystem.dto.Message;
import net.prostars.messagesystem.entity.MessageEntity;
import net.prostars.messagesystem.repository.MessageRepository;
import net.prostars.messagesystem.session.WebSocketSessionManager;
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
    /**
     * JSON과 Java 객체를 상호 변환한다.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 현재 연결된 WebSocket Session을 관리한다.
     */
    private final WebSocketSessionManager webSocketSessionManager;
    /**
     * 수신한 채팅 메시지를 MySQL에 저장한다.
     */
    private final MessageRepository messageRepository;

    /**
     * WebSocket 메시지 처리에 필요한 의존성을 주입한다.
     *
     * @param webSocketSessionManager 연결된 WebSocket Session 관리 객체
     * @param messageRepository       채팅 메시지 저장 Repository
     */
    public MessageHandler(WebSocketSessionManager webSocketSessionManager, MessageRepository messageRepository) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.messageRepository = messageRepository;
    }

    /**
     * WebSocket 연결이 성공하면 호출된다.
     *
     * 연결된 Session을 ConcurrentWebSocketSessionDecorator로 감싼 뒤
     * SessionManager에 등록한다.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("ConnectionEstablished : {}", session.getId());

        // Thread-Safe한 메시지 전송을 위해 Decorator 적용
        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator =
                new ConcurrentWebSocketSessionDecorator(session, 5000, 100 * 1024);
        // Session 등록
        webSocketSessionManager.storeSession(concurrentWebSocketSessionDecorator);

    }

    /**
     * WebSocket 통신 중 전송 오류가 발생하면 호출된다.
     *
     * 오류가 발생한 Session을 제거하고 연결을 종료한다.
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("TransportError: [{}] from {}", exception.getMessage(), session.getId());
        // 전송 오류 발생 시 Session 제거
        webSocketSessionManager.terminateSession(session.getId());
    }


    /**
     * WebSocket 연결이 정상적으로 종료되면 호출된다.
     *
     * 종료된 Session을 SessionManager에서 제거하고
     * 해당 WebSocket 연결을 정리한다.
     *
     * @param session 종료된 WebSocket Session
     * @param status  연결 종료 상태 정보
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        log.info("ConnectionClosed: [{}] from {}", status, session.getId());
        // 연결 종료 시 Session 제거
        webSocketSessionManager.terminateSession(session.getId());
    }

    /**
     * 클라이언트가 TextMessage를 전송하면 호출된다.
     *
     * 처리 순서:
     * JSON 역직렬화 → 메시지 DB 저장 → 다른 참여자에게 전달
     */
    @Override
    protected void handleTextMessage(WebSocketSession senderSession, @NonNull TextMessage message) throws Exception {
        log.info("Received TextMessage: [{}] from {}", message, senderSession.getId());
        String payload = message.getPayload();
        try {
            // 클라이언트의 JSON을 채팅 Message 객체로 변환한다.
            Message receivedMessage = objectMapper.readValue(payload, Message.class);
            // 수신한 메시지를 Entity로 변환하여 DB에 저장한다.
            messageRepository.save(new MessageEntity(receivedMessage.username(), receivedMessage.content()));

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

    /**
     * 지정한 WebSocket Session으로 메시지를 전송한다.
     *
     * Message 객체를 JSON으로 직렬화한 뒤 TextMessage로 전달한다.
     *
     * @param session 메시지를 받을 WebSocket Session
     * @param message 전송할 채팅 메시지
     */
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
