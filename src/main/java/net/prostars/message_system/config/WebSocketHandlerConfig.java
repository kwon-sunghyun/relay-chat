package net.prostars.message_system.config;

import net.prostars.message_system.handler.MessageHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Endpoint를 등록하는 설정 클래스.
 *
 * <p>Spring Boot에서 WebSocket 요청이 들어오면
 * 지정한 Handler가 해당 요청을 처리하도록 매핑한다.</p>
 *
 * <p>클라이언트는 "/ws/v1/message" Endpoint로 연결을 요청하며,
 * 연결 이후 발생하는 이벤트(연결, 메시지 수신, 종료)는
 * MessageHandler에서 처리된다.</p>
 */
@Configuration
@EnableWebSocket
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    /**
     * WebSocket 메시지를 처리하는 Handler.
     */
    private final MessageHandler messageHandler;

    public WebSocketHandlerConfig(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * WebSocket Endpoint를 등록한다.
     *
     * <p>
     * ws://host/ws/v1/message 로 연결되는 모든 WebSocket 요청은
     * MessageHandler가 처리한다.
     * </p>
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageHandler, "/ws/v1/message");
    }
}
