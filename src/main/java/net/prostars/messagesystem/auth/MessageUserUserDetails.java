package net.prostars.messagesystem.auth;

import com.fasterxml.jackson.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Spring Security에서 사용할 사용자 인증 정보 객체.
 * DB의 사용자 정보를 Security가 이해할 수 있는 형태로 전달한다.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageUserUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private String password;

    @JsonCreator
    public MessageUserUserDetails(
            @JsonProperty("userId")
            Long userId,
            @JsonProperty("username")
            String username,
            @JsonProperty("password")
            String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public void erasePassword() {
        password= "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MessageUserUserDetails that = (MessageUserUserDetails) object;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
