package com.winter.winter_back.common.security.svc.impl;

import tools.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  로그인 되지 않은 상태에서 url 접근할 경우 처리 (인증 실패 이후 처리)
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;
	private final ExceptionWrapperUtil exceptionInfoConfig;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
			throws IOException {

		log.info("######## 로그인 필요 #######");


		// 응답 생성
		ResponseBaseDto comResultDto = exceptionInfoConfig.makeResponse("login_required");
		response.setStatus(comResultDto.getStatus());
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().print(objectMapper.writeValueAsString(comResultDto));
		response.getWriter().flush();
	}
}