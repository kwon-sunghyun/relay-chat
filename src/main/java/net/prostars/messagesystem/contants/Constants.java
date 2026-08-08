package net.prostars.messagesystem.contants;

/**
 * 애플리케이션에서 공통으로 사용하는 상수를 관리한다.
 */
public enum Constants {

    // WebSocket Session에 저장할 HTTP Session ID의 Key
    HTTP_SESSION_ID("HTTP_SESSION_ID");

    private final String value;

    Constants(String value) {
        this.value = value;
    }

    /**
     * 상수의 실제 문자열 값을 반환한다.
     */
    public String getValue() {
        return value;
    }
}
