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
@Table(name = "message_user")
public class MessageUserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userid;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    public MessageUserEntity() {

    }

    public MessageUserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getUserid() {
        return userid;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }


    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MessageUserEntity that = (MessageUserEntity) object;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public String toString() {
        return String.format("MessageUserEntity{userid=%d, username='%s', createdAt=%s, updatedAt=%s}",
                userid, username, getCreatedAt(), getUpdatedAt());
    }
}
