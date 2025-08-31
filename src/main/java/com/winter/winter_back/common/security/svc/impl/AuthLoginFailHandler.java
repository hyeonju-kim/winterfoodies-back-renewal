package com.winter.winter_back.common.security.svc.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import com.winter.winter_back.common.exception.ExceptionWrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  로그인 실패 시 처리 (잘못된 아이디나 비밀번호 입력 시)
 */
@Slf4j
@RequiredArgsConstructor
public class AuthLoginFailHandler extends SimpleUrlAuthenticationFailureHandler {

	private final ObjectMapper objectMapper;
	private final ExceptionWrapperUtil exceptionInfoConfig;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
										HttpServletResponse response,
										AuthenticationException exception) throws IOException {
		ResponseBaseDto comResultDto = exceptionInfoConfig.makeResponse("login_fail");
		log.info("##### 로그인 실패 ######");

		// 인증 실패 응답을 정의 (HTML 또는 JSON)
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().print(objectMapper.writeValueAsString(comResultDto));
		response.getWriter().flush();
	}
}
