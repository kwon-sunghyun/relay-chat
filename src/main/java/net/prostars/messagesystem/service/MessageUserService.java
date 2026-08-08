package net.prostars.messagesystem.service;

import jakarta.transaction.Transactional;
import net.prostars.messagesystem.dto.domain.UserId;
import net.prostars.messagesystem.entity.MessageUserEntity;
import net.prostars.messagesystem.repository.MessageUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 회원가입과 회원탈퇴 비즈니스 로직을 처리하는 서비스.
 *
 * 비밀번호 암호화, 사용자 저장/삭제,
 * 현재 로그인 사용자 조회를 담당한다.
 */
@Service
public class MessageUserService {

    private static final Logger log = LoggerFactory.getLogger(MessageUserService.class);
    private final SessionService sessionService;
    private final MessageUserRepository messageUserRepository;
    private final PasswordEncoder passwordEncoder;

    public MessageUserService(
            SessionService sessionService,
            MessageUserRepository messageUserRepository,
            PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.messageUserRepository = messageUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserId addUser(String username, String password) {
        MessageUserEntity messageUserEntity = messageUserRepository.save(new MessageUserEntity(username, passwordEncoder.encode(password)));
        log.info("User registered. UserId: {}, Username: {}", messageUserEntity.getUserid(), messageUserEntity.getUsername());
        return new UserId(messageUserEntity.getUserid());
    }

    @Transactional
    public void removeUser() {
        String username = sessionService.getUsername();
        MessageUserEntity messageUserEntity = messageUserRepository.findByUsername(username).orElseThrow();
        messageUserRepository.deleteById(messageUserEntity.getUserid());
        log.info("User unregistered. UserId: {}, Username: {}", messageUserEntity.getUserid(), messageUserEntity.getUsername());

    }
}
