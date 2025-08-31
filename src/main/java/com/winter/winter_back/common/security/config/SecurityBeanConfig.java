package com.winter.winter_back.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import com.winter.winter_back.common.security.dto.ComSecurityInfoDto;
import com.winter.winter_back.common.security.svc.SecuritySupportSvc;
import com.winter.winter_back.common.security.svc.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
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
	private ComSecurityInfoDto comSecurityInfoDto;

	@Autowired
	@Lazy
	public SecurityBeanConfig(ComSecurityInfoDto comSecurityInfoDto) {
		this.comSecurityInfoDto = comSecurityInfoDto;
	}


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
	public AuthDynamicCreateUserHandler getAuthDynamicCreateUserHandler(AuthenticationManager authenticationManager) {
		return new AuthDynamicCreateUserHandler(authenticationManager);
	}

	@Bean
	public AuthenticationEntryPointImpl getAuthenticationEntryPointImpl(ObjectMapper objectMapper, ExceptionWrapperUtil exceptionInfoConfig) {
		return new AuthenticationEntryPointImpl(objectMapper, exceptionInfoConfig);
	}

	@Bean
	public AuthLoginFailHandler getAuthLoginFailHandler(ObjectMapper objectMapper, ExceptionWrapperUtil exceptionInfoConfig) {
		return new AuthLoginFailHandler(objectMapper, exceptionInfoConfig);
	}

//	@Bean
//	public AuthLoginProvider getAuthLoginProvider(SecuritySupportSvc securitySupportSvc, PasswordEncoder passwordEncoder) {
//		return new AuthLoginProvider(securitySupportSvc, passwordEncoder, comSecurityInfoDto.getLogin().getSecretKey());
//	}
//
	@Bean
	public AuthLoginSuccessHandler getAuthLoginSuccessHandler(ObjectMapper objectMapper, Environment env, ExceptionWrapperUtil exceptionWrapperUtil) {
		return new AuthLoginSuccessHandler(objectMapper, env, exceptionWrapperUtil);
	}

	@Bean
	public AuthLogoutHandler getAuthLogoutHandler() {
		return new AuthLogoutHandler(comSecurityInfoDto.getLogout().getSuccessUrl());
	}

	@Bean("authorizationChecker")
	public AuthorizationChecker getAuthorizationChecker(SecuritySupportSvc securitySupportSvc) {
		return new AuthorizationChecker(securitySupportSvc, comSecurityInfoDto.getApplicationName());
	}
}
