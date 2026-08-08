package net.prostars.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * WebSocket 요청 메시지의 공통 부모 클래스.
 *
 * JSON의 type 값을 기준으로
 * MessageRequest 또는 KeepAliveRequest로 변환된다.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageRequest.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = KeepAliveRequest.class, name = "KEEP_ALIVE")
})
public abstract class BaseRequest {
    private final String type;

    public BaseRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}