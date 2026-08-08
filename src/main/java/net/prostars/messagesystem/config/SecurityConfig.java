package net.prostars.messagesystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.prostars.messagesystem.auth.RestApiLoginAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

/**
 * Spring Security 인증 및 인가 설정 클래스.
 *
 * 주요 설정:
 *
 * 1. BCrypt 비밀번호 암호화
 * 2. 테스트 사용자 등록
 * 3. AuthenticationManager 구성
 * 4. JSON 로그인 필터 등록
 * 5. URL별 접근 권한 설정
 * 6. 로그아웃 처리
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 비밀번호를 단방향 해시로 암호화하고 비교하는 객체.
     *
     * BCrypt는 같은 비밀번호를 암호화해도
     * 매번 서로 다른 결과가 만들어질 수 있다.
     *
     * 로그인 시에는 평문 비밀번호를 다시 복호화하지 않고
     * matches()를 통해 저장된 해시값과 비교한다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security의 인증 관리자 구성.
     *
     * AuthenticationManager는 전달받은 Authentication 객체를
     * 적절한 AuthenticationProvider에 전달한다.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService detailsService, PasswordEncoder passwordEncoder) {

        // DaoAuthenticationProvider는 아이디와 비밀번호 기반 인증을 처리한다.
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        // 사용자 조회를 담당하는 UserDetailsService를 등록한다.
        daoAuthenticationProvider.setUserDetailsService(detailsService);
        // 비밀번호 비교에 사용할 PasswordEncoder를 등록한다.
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        // ProviderManager는 AuthenticationManager의 대표적인 구현체다.
        return new ProviderManager(daoAuthenticationProvider);
    }

    /**
     * Spring Security의 HTTP 보안 필터 체인을 구성한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {

        // JSON 로그인 요청을 처리할 커스텀 필터를 생성한다.
        RestApiLoginAuthFilter restApiLoginAuthFilter =
                new RestApiLoginAuthFilter(
                        new AntPathRequestMatcher("/api/v1/auth/login", "POST"), authenticationManager);

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterAt(restApiLoginAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/api/v1/auth/register", "/api/v1/auth/login")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .logout(
                        logout ->
                                logout.logoutUrl("/api/v1/auth/logout").logoutSuccessHandler(this::logoutHandler));

        return httpSecurity.build();
    }

    /**
     * 로그아웃 처리 이후 클라이언트에 반환할 응답을 구성한다.
     *
     * Spring Security의 LogoutFilter는 기본적으로 다음 작업을 수행한다.
     *
     * - HttpSession 무효화
     * - SecurityContext 제거
     * - Authentication 제거
     * - JSESSIONID 관련 로그인 상태 제거
     */
    private void logoutHandler(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // 응답 형식과 문자 인코딩을 설정한다.
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding("UTF-8");
        String message;

        // 로그아웃 처리 당시 인증 정보가 존재하는지 확인한다.
        if (authentication != null && authentication.isAuthenticated()) {
            response.setStatus(HttpStatus.OK.value());
            message = "Logout success.";
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            message = "Logout failed.";
        }

        // 로그아웃 결과를 응답 Body에 작성한다.
        try {
            response.getWriter().write(message);
        } catch (IOException ex) {
            log.error("Response failed. cause: {}", ex.getMessage());
        }
    }
}