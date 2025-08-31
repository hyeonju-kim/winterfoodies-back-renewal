package com.winter.winter_back.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * description    : RestTemplate 설정
 * fileName       : RestTemplateConfig
 * author         : 문다은
 * date           : 2024-07-11
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-07-11        문다은              최초 생성
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {
    private final RestTemplateInterceptor restTemplateInterceptor;
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalInterceptors(restTemplateInterceptor)
                .build();
    }
}
