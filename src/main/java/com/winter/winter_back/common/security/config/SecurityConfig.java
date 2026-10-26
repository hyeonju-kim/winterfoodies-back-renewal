package com.winter.winter_back.common.security.config;

import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import com.winter.winter_back.common.security.dto.ComSecurityInfoDto;
import com.winter.winter_back.common.security.svc.impl.AuthAccessDeniedHandler;
import com.winter.winter_back.common.security.svc.impl.AuthenticationEntryPointImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final ComSecurityInfoDto comSecurityInfoDto; // application.yml과 매핑 DTO
    private final ExceptionWrapperUtil exceptionWrapperUtil;
    private final AuthenticationEntryPointImpl authenticationEntryPointImpl;
    private final AuthAccessDeniedHandler authAccessDeniedHandler;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-time.access:30}")
    private int accessTokenExpireMinute;

    @Value("${jwt.expire-time.refresh:1440}")
    private int refreshTokenExpireMinute;

    @Value("${cors.allowed-origin-patterns}")
    private List<String> corsAllowedOriginPatterns;

    @Bean
    public TokenFilter tokenFilter() {
        return new TokenFilter(exceptionWrapperUtil, objectMapper);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 인증은 Authorization 헤더(JWT)로만 하므로 쿠키 전송(credentials)은 허용하지 않는다
        configuration.setAllowCredentials(false);
        configuration.setAllowedOriginPatterns(corsAllowedOriginPatterns);
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 정적 리소스 등 보안 필터 체인을 아예 거치지 않을 경로
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        String[] staticContentsUrlPatterns = comSecurityInfoDto.getStaticContentsUrlPatterns();
        return web -> {
            if (staticContentsUrlPatterns != null && staticContentsUrlPatterns.length > 0) {
                web.ignoring().requestMatchers(staticContentsUrlPatterns);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TokenProvider 초기화
        TokenProvider.build(
                secret,
                "winter_user_auth",
                accessTokenExpireMinute,
                refreshTokenExpireMinute
        );

        TokenProvider.setRedisTemplate(redisTemplate);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPointImpl)
                        .accessDeniedHandler(authAccessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 허용하는 경로는 application.yml 에서 메서드별로 관리 (조회는 GET 만, 로그인·가입은 POST 만)
                        .requestMatchers(nullToEmpty(comSecurityInfoDto.getPermitAllUrlPatterns())).permitAll()
                        .requestMatchers(HttpMethod.GET, nullToEmpty(comSecurityInfoDto.getPermitAllGetUrlPatterns())).permitAll()
                        .requestMatchers(HttpMethod.POST, nullToEmpty(comSecurityInfoDto.getPermitAllPostUrlPatterns())).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static String[] nullToEmpty(String[] patterns) {
        return patterns == null ? new String[0] : patterns;
    }
}
