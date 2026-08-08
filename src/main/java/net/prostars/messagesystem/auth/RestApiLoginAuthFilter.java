package net.prostars.messagesystem.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.prostars.messagesystem.dto.restapi.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JSON 형식의 로그인 요청을 처리하는 인증 Filter.
 *
 * username/password를 읽어
 * Spring Security의 AuthenticationManager에 인증을 요청한다.
 */
public class RestApiLoginAuthFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * HTTP 요청 Body의 JSON을 Java 객체로 변환하기 위한 Jackson 객체.
     * <p>
     * 예:
     * <p>
     * {
     * "username": "testuser",
     * "password": "testpass"
     * }
     * <p>
     * 위 JSON을 LoginRequest 객체로 변환한다.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 로그인 필터 생성자.
     *
     * @param requiresAuthenticationRequestMatcher 이 필터가 처리할 URL과 HTTP Method 조건
     * @param authenticationManager                실제 인증 처리를 담당하는 AuthenticationManager
     */
    public RestApiLoginAuthFilter(RequestMatcher requiresAuthenticationRequestMatcher, AuthenticationManager authenticationManager) {
        super(requiresAuthenticationRequestMatcher, authenticationManager);
    }

    /**
     * 로그인 요청에서 아이디와 비밀번호를 읽어 인증을 시도한다.
     * <p>
     * 이 메서드는 필터의 RequestMatcher 조건과 일치하는 요청이 들어왔을 때 호출된다.
     * <p>
     * 인증 성공:
     * Authentication 객체 반환
     * <p>
     * 인증 실패:
     * AuthenticationException 발생
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (!request.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("지원하지 않는 타입 : " + request.getContentType());
        }
        LoginRequest loginRequest =
                objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    /**
     * 인증 성공 후 실행되는 메서드.
     * <p>
     * 인증된 Authentication 객체를 SecurityContext에 저장하고,
     * SecurityContext를 HttpSession에 저장한다.
     * <p>
     * 이를 통해 이후 요청에서도 로그인 상태를 유지할 수 있다.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        ((MessageUserUserDetails) authResult.getPrincipal()).erasePassword();
        securityContext.setAuthentication(authResult);
        HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
        contextRepository.saveContext(securityContext, request, response);

        String sessionId = request.getSession().getId();
        String encodedSessionId = Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));

        // 로그인 성공 응답을 HTTP 200으로 설정한다.
        response.setStatus(HttpServletResponse.SC_OK);
        // 응답 본문을 일반 문자열 형식으로 설정한다.
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write(encodedSessionId);
        // 작성한 응답 데이터를 즉시 클라이언트로 전송한다.
        response.getWriter().flush();
    }

    /**
     * 인증 실패 후 실행되는 메서드.
     * <p>
     * 아이디가 존재하지 않거나 비밀번호가 일치하지 않는 경우 호출된다.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 응답 형식을 일반 문자열로 설정한다.
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        // 인증 실패 메시지를 응답 Body로 반환한다.
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("인증 실패");
        response.getWriter().flush();
    }
}

