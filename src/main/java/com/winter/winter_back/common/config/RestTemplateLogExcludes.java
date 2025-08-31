package com.winter.winter_back.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description    : RestTemplate 로그 표시 x
 * fileName       : RestTemplateLogExcludes
 * author         : mde
 * date           : 2025-07-15
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-07-15           mde               최초 생성
 */
@Component
@ConfigurationProperties(prefix = "cnf.logging.excludes")
@Data
public class RestTemplateLogExcludes {
    private List<String> path;
}
