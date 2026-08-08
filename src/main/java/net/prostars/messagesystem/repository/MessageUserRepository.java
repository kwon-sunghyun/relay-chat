package net.prostars.messagesystem.repository;

import net.prostars.messagesystem.entity.MessageUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * message_user 테이블에 접근하는 JPA Repository.
 *
 * 기본 CRUD 기능과 username 기반 사용자 조회 기능을 제공한다.
 */
@Repository
public interface MessageUserRepository
        extends JpaRepository<MessageUserEntity, Long> {

    /**
     * username으로 사용자를 조회한다.
     */
    Optional<MessageUserEntity> findByUsername(
            @NonNull String username
    );
}
