package net.prostars.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.prostars.messagesystem.contants.MessageType;

/**
 * 클라이언트가 전송한 채팅 메시지를 표현하는 WebSocket 요청 DTO.
 */
public class MessageRequest extends BaseRequest{

    private final String username;
    private final String content;

    @JsonCreator
    public MessageRequest(
            @JsonProperty("username")
            String username,
            @JsonProperty("content")
            String content) {
        super(MessageType.MESSAGE);
        this.username = username;
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }
}
