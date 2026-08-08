package net.prostars.messagesystem.dto.restapi;

/**
 * 회원가입 요청의 username/password를 전달하는 DTO.
 */
public record UserRegisterRequest(
        String username,
        String password
) {
}