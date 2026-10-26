package com.winter.winter_back.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import com.winter.winter_back.common.security.dto.ComSecurityInfoDto;
import com.winter.winter_back.common.security.svc.impl.AuthAccessDeniedHandler;
import com.winter.winter_back.common.security.svc.impl.AuthenticationEntryPointImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * @systemName : abaus-sample-web
 * @programName : SecurityBeanConfig
 * @author : 이성진
 * @createDate : 2021. 9. 7.
 * @description :
 */
@Configuration
public class SecurityBeanConfig {

	@Bean
	@ConfigurationProperties("spring.security")
	public ComSecurityInfoDto getComSecurityInfoDto() {
		return new ComSecurityInfoDto();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthAccessDeniedHandler getAuthAccessDeniedHandler(ObjectMapper objectMapper, ExceptionWrapperUtil exceptionInfoConfig) {
		return new AuthAccessDeniedHandler(objectMapper, exceptionInfoConfig);
	}

	@Bean
	public AuthenticationEntryPointImpl getAuthenticationEntryPointImpl(ObjectMapper objectMapper, ExceptionWrapperUtil exceptionInfoConfig) {
		return new AuthenticationEntryPointImpl(objectMapper, exceptionInfoConfig);
	}
}
