package net.prostars.message_system.dto;

/**
 * WebSocket을 통해 송수신되는 메시지 DTO.
 *
 * @param content 실제 채팅 메시지 내용
 */
public record Message(String username ,String content) {
}
