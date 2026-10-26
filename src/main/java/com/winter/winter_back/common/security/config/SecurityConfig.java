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

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final ComSecurityInfoDto comSecurityInfoDto; // application.yml과 매핑 DTO
    private final ExceptionWrapperUtil exceptionWrapperUtil;
    private final AuthenticationEntryPointImpl authenticationEntryPointImpl;
    private final AuthAccessDeniedHandler authAccessDeniedHandler;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-time.access:30}")
    private int accessTokenExpireMinute;

    @Value("${jwt.expire-time.refresh:1440}")
    private int refreshTokenExpireMinute;

    @Bean
    public TokenFilter tokenFilter() {
        return new TokenFilter(exceptionWrapperUtil);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("*");
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
                        .requestMatchers(comSecurityInfoDto.getPermitAllUrlPatterns()).permitAll() // 누구든 접근 허용
                        .requestMatchers("/login", "/auth/login", "/register", "/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
