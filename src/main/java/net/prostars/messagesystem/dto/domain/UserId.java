package net.prostars.messagesystem.dto.domain;

/**
 * 사용자 ID를 표현하는 Value Object.
 *
 * null 또는 0 이하의 잘못된 ID 생성을 방지한다.
 */
public record UserId(Long id) {

    /**
     * UserId 생성 시 값의 유효성을 검사한다.
     */
    public UserId {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid UserId");
        }
    }
}