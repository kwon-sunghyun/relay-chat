package net.prostars.messagesystem.handler

import com.fasterxml.jackson.databind.ObjectMapper
import net.prostars.messagesystem.MessageSystemApplication
import net.prostars.messagesystem.dto.websocket.inbound.MessageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import spock.lang.Specification

import org.springframework.http.HttpHeaders
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * 그룹 채팅 통합 테스트.
 *
 * 실제 WebSocket Server를 실행한 뒤
 * 여러 Client가 연결하여
 * 메시지가 정상적으로 브로드캐스트되는지 검증한다.
 */
@SpringBootTest(classes = MessageSystemApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageHandlerSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 그룹 Client가 연결된 상태에서
     * 서로 메시지를 정상적으로 주고받는지 검증한다.
     */
    def "Group Chat Basic Test"() {
        // WebSocket Endpoint 생성
        // Client A, B, C 연결
        given:
        register("testuserA", "testpassA")
        register("testuserB", "testpassB")
        register("testuserC", "testpassC")
        def sessionIdA = login("testuserA", "testpassA")
        def sessionIdB = login("testuserB", "testpassB")
        def sessionIdC = login("testuserC", "testpassC")
        // WebSocket Endpoint

        def (clientA, clientB, clientC) = [createClient(sessionIdA), createClient(sessionIdB), createClient(sessionIdC)]

        // 각 Client가 메시지 전송
        when:
        clientA.session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(new MessageRequest("clientA", "안녕하세요. A 입니다."))
        ))
        clientB.session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(new MessageRequest("clientB", "안녕하세요. B 입니다."))
        ))
        clientC.session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(new MessageRequest("clientC", "안녕하세요. C 입니다."))
        ))

        // 각 Client는 자신의 메시지를 제외한
        // 다른 Client들의 메시지를 수신해야 한다.
        then:
        def resultA = clientA.queue.poll(1, TimeUnit.SECONDS) + clientA.queue.poll(1, TimeUnit.SECONDS)
        def resultB = clientB.queue.poll(1, TimeUnit.SECONDS) + clientB.queue.poll(1, TimeUnit.SECONDS)
        def resultC = clientC.queue.poll(1, TimeUnit.SECONDS) + clientC.queue.poll(1, TimeUnit.SECONDS)

        resultA.contains("clientB") && resultA.contains("clientC")
        resultB.contains("clientA") && resultB.contains("clientC")
        resultC.contains("clientA") && resultC.contains("clientB")

        // 추가 메시지가 없는지 확인
        and:
        clientA.queue.isEmpty()
        clientB.queue.isEmpty()
        clientC.queue.isEmpty()

        // 테스트 종료 후 WebSocket 연결 종료
        cleanup:
        unregister(sessionIdA)
        unregister(sessionIdB)
        unregister(sessionIdC)
        clientA.session?.close()
        clientB.session?.close()
        clientC.session?.close()

    }

    def register(String username, String password) {
        def url = "http://localhost:${port}/api/v1/auth/register"
        def headers = new HttpHeaders(["Content-Type": "application/json"])
        def jsonBody = objectMapper.writeValueAsString([username: username, password: password])
        def httpEntity = new HttpEntity(jsonBody, headers);
        try {
            new RestTemplate().exchange(url, HttpMethod.POST, httpEntity, String)
        } catch (Exception ignore) {

        }
    }

    def unregister(String sessionId) {
        def url = "http://localhost:${port}/api/v1/auth/unregister"
        def headers = new HttpHeaders()
        headers.add("Content-Type", "application/json")
        headers.add("Cookie", "SESSION=${sessionId}")
        def httpEntity = new HttpEntity(headers);
        def responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, httpEntity, String)
        responseEntity.body

    }

    def login(String username, String password) {
        def url = "http://localhost:${port}/api/v1/auth/login"
        def headers = new HttpHeaders(["Content-Type": "application/json"])
        def jsonBody = objectMapper.writeValueAsString([username: username, password: password])
        def httpEntity = new HttpEntity(jsonBody, headers);
        def responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, httpEntity, String)
        def sessionId = responseEntity.body
        sessionId

    }

    // 테스트용 WebSocket Client 생성
    // 수신한 메시지를 Queue에 저장
    // Client 연결 후 Session 반환
    def createClient(String sessionId) {
        def url = "ws://localhost:${port}/ws/v1/message"
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(5)
        def webSocketHttpHeaders = new WebSocketHttpHeaders()
        webSocketHttpHeaders.add("Cookie", "SESSION=${sessionId}")

        // 첫 번째 Client 연결
        def client = new StandardWebSocketClient()
        def webSocketSession = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                blockingQueue.put(message.payload)
            }
        }, webSocketHttpHeaders, new URI(url)).get()

        return [queue: blockingQueue, session: webSocketSession]

    }
}
