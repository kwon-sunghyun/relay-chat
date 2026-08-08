package net.prostars.messagesystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.FlushMode;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session을 Redis에 저장하도록 설정하는 클래스.
 *
 * HTTP Session 정보를 Redis에 저장하고,
 * Spring Security 객체도 JSON 형태로 직렬화한다.
 */
@Configuration
@EnableRedisHttpSession(redisNamespace = "message:user_session", maxInactiveIntervalInSeconds = 300, flushMode = FlushMode.IMMEDIATE)
public class RedisSessionConfig {

    /**
     * Spring Session을 Redis에 저장하도록 설정하는 클래스.
     *
     * HTTP Session 정보를 Redis에 저장하고,
     * Spring Security 객체도 JSON 형태로 직렬화한다.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
