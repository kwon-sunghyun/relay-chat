package net.prostars.messagesystem.entity;

import jakarta.persistence.*;

import java.util.Objects;


/**
 * message 테이블과 매핑되는 채팅 메시지 Entity.
 * <p>
 * 사용자가 전송한 이름과 메시지 내용,
 * 생성·수정 시간을 데이터베이스에 저장한다.
 */
@Entity
@Table(name = "message")
public class MessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence")
    private Long messageSequence;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "content", nullable = false)
    private String content;

    public MessageEntity() {

    }

    public MessageEntity(String username, String content) {
        this.username = username;
        this.content = content;
    }

    public Long getMessageSequence() {
        return messageSequence;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }


    /**
     * DB 식별자인 messageSequence를 기준으로 Entity를 비교한다.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntity that = (MessageEntity) o;
        return Objects.equals(messageSequence, that.messageSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageSequence);
    }

    @Override
    public String toString() {
        return "MessageEntity{messageSequence=%d, username='%s', content='%s', createdAt=%s, updatedAt=%s}"
                .formatted(messageSequence, username, content, getCreatedAt(), getUpdatedAt());
    }
}
