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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
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

    // AuthenticationManager Bean 추가
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }



    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(comSecurityInfoDto.getStaticContentsUrlPatterns());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // TokenProvider 초기화
        TokenProvider.build(
                secret,
                "winter_user_auth",
                accessTokenExpireMinute,
                refreshTokenExpireMinute
        );

        TokenProvider.setRedisTemplate(redisTemplate);

        http.authorizeRequests()
                .antMatchers(comSecurityInfoDto.getPermitAllUrlPatterns()).permitAll(); // 누구든 접근 허용

        http.csrf().disable()
                .addFilter(corsFilter())
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPointImpl)
                .accessDeniedHandler(authAccessDeniedHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/login", "/auth/login", "/register", "/health").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}