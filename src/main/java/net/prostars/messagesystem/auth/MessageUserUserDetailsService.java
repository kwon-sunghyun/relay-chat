package net.prostars.messagesystem.auth;

import net.prostars.messagesystem.entity.MessageUserEntity;
import net.prostars.messagesystem.repository.MessageUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * username으로 DB에서 사용자를 조회하여
 * Spring Security의 UserDetails로 변환하는 서비스.
 */
@Service
public class MessageUserUserDetailsService implements UserDetailsService {


    private static final Logger log = LoggerFactory.getLogger(MessageUserUserDetailsService.class);
    private final MessageUserRepository messageUserRepository;

    public MessageUserUserDetailsService(MessageUserRepository messageUserRepository) {
        this.messageUserRepository = messageUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MessageUserEntity messageUserEntity = messageUserRepository.findByUsername(username).orElseThrow(() -> {
            log.info("User not found: {}", username);
            return new UsernameNotFoundException("");
        });
        return  new MessageUserUserDetails(
                messageUserEntity.getUserid(),
                messageUserEntity.getUsername(),
                messageUserEntity.getPassword());
    }
}
