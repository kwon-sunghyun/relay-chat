package net.prostars.message_system.handler

import com.fasterxml.jackson.databind.ObjectMapper
import net.prostars.message_system.MessageSystemApplication
import net.prostars.message_system.dto.Message
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * MessageHandler 통합 테스트.
 *
 * 실제 WebSocket Server를 실행한 뒤
 * 두 Client가 연결하여
 * 메시지가 정상적으로 전달되는지 검증한다.
 */
@SpringBootTest(classes = MessageSystemApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageHandlerSpec extends Specification {

    @LocalServerPort
    private int port

    private ObjectMapper objectMapper = new ObjectMapper()

    /**
     * 두 Client가 연결된 상태에서
     * 서로 메시지를 정상적으로 주고받는지 검증한다.
     */
    def "Direct Chat Basic Test"() {
        given:
        // WebSocket Endpoint
        def url = "ws://localhost:${port}/ws/v1/message"
        // 수신 메시지 검증용 Queue
        BlockingQueue<String> leftQueue = new ArrayBlockingQueue<>(1)
        BlockingQueue<String> rightQueue = new ArrayBlockingQueue<>(1)

        // 첫 번째 Client 연결
        def leftClient = new StandardWebSocketClient()
        def leftWebSocketSession = leftClient.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                leftQueue.put(message.payload)
            }
        }, url).get()

        // 두 번째 Client 연결
        def rightClient = new StandardWebSocketClient()
        def rightWebSocketSession = rightClient.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                rightQueue.put(message.payload)
            }
        }, url).get()

        // 양쪽 Client가 각각 메시지 전송
        when:
        leftWebSocketSession.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(new Message("안녕하세요."))
        ))
        rightWebSocketSession.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(new Message("Hello."))
        ))

        // 상대방에게 메시지가 정상 전달되는지 검증
        then:
        rightQueue.poll(1, TimeUnit.SECONDS).concat("안녕하세요.")

        and:
        leftQueue.poll(1, TimeUnit.SECONDS).concat("Hello.")

        cleanup:
        leftWebSocketSession?.close()
        rightWebSocketSession?.close()
    }
}
