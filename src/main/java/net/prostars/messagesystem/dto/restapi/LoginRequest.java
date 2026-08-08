package net.prostars.messagesystem.dto.restapi;

/**
 * 로그인 요청 JSON을 전달받기 위한 DTO.
 *
 * 요청 JSON:
 *
 * {
 *   "username": "testuser",
 *   "password": "testpass"
 * }
 *
 * Java record는 다음 요소를 자동으로 생성한다.
 *
 * - 생성자
 * - username()
 * - password()
 * - equals()
 * - hashCode()
 * - toString()
 *
 * 로그인 요청 데이터를 전달하는 단순 DTO이므로
 * 불변 객체인 record를 사용했다.
 */
public record LoginRequest(String username, String password) {
}
