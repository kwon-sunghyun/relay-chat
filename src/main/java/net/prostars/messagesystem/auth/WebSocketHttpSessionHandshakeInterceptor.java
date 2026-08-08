package net.prostars.messagesystem.auth;

import jakarta.servlet.http.HttpSession;
import net.prostars.messagesystem.contants.Constants;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 연결 전에 HttpSession을 확인한다.
 *
 * 세션이 있으면 Handshake를 허용하고,
 * 없으면 401 응답으로 연결을 거부한다.
 */
@Component
public class WebSocketHttpSessionHandshakeInterceptor extends HttpSessionHandshakeInterceptor {


    private static final Logger log = LoggerFactory.getLogger(WebSocketHttpSessionHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            HttpSession httpSession = servletServerHttpRequest.getServletRequest().getSession(false);
            if (httpSession != null) {
                attributes.put(Constants.HTTP_SESSION_ID.getValue(), httpSession.getId());
                return true;
            } else {
                log.info("WebSocket Handshake failed. httpSession is null");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        } else {
            log.info("WebSocket Handshake failed. request: {}", request.getClass());
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
    }
}
