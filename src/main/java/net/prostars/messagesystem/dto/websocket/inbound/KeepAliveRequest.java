package net.prostars.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import net.prostars.messagesystem.contants.MessageType;

/**
 * WebSocket 연결 유지를 위한 KeepAlive 요청.
 */
public class KeepAliveRequest extends BaseRequest {

    /**
     * JSON을 KeepAliveRequest로 변환할 때 사용하는 생성자.
     */
    @JsonCreator
    public KeepAliveRequest() {
        super(MessageType.KEEP_ALIVE);
    }
}