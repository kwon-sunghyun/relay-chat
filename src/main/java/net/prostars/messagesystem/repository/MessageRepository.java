package net.prostars.messagesystem.repository;

import net.prostars.messagesystem.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 채팅 메시지의 저장·조회·수정·삭제를 담당하는 Repository.
 *
 * JpaRepository를 상속하여 기본 CRUD 기능을 사용한다.
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

}
