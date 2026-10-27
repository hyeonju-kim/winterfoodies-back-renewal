package com.winter.winter_back.common.config;

import com.winter.winter_back.common.file.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * description    : 웹 공통 설정 파일
 * fileName       : WebConfig
 * date           : 25. 9. 15.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 9. 15.         hj kim             최초 생성
 * 26. 10. 26.       hj kim             올린 사진(/uploads/**) 제공 추가
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ImageStorage imageStorage;

    // 올린 사진: /uploads/** → 사진 저장 폴더 (로그인 없이 조회, application.yml static-contents-url-patterns)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ImageStorage.URL_PREFIX + "**")
                .addResourceLocations(imageStorage.getRootDirectory().toUri().toString());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해
                .allowedOriginPatterns("*")  // 모든 오리진 허용 (개발용)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")  // 허용할 HTTP 메서드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true)  // 쿠키 포함 요청 허용
                .exposedHeaders("*")
                .maxAge(3600);  // preflight 요청 캐시 시간 (1시간)
    }
}